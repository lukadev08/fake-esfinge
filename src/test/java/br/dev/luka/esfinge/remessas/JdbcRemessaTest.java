package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.*;
import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.domain.*;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.configuration.*;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.application.RemessaService;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class JdbcRemessaTest {
    @TempDir Path temporary;
    static final Instant INITIAL=Instant.parse("2026-01-01T00:00:00Z");
    SimulatorSettings settings(String scenario) {
        return new SimulatorSettings(temporary.resolve("state.sqlite").toString(),scenario,"2026/V6/6.06","admin-ficticio","fixed",1,INITIAL,Duration.ofHours(1),
                List.of(new SimulatorSettings.User("usuario-ficticio","acesso-ficticio","senha-ficticia",Set.of("0001","0002"))));
    }
    record Fixture(DataSource source,AuthenticationService authentication,RemessaService service) {
        String token(String ug) { return authentication.login("acesso-ficticio","senha-ficticia",ug); }
    }
    Fixture fixture(String scenario) { return fixture(SqliteDatabase.open(settings(scenario)),scenario,Clock.fixed(INITIAL,ZoneOffset.UTC)); }
    Fixture fixture(DataSource source,String scenario,Clock clock) {
        var authentication=new AuthenticationService(new JdbcAuthenticationRepository(source),clock,scenario,Duration.ofHours(1));
        var inspection=new ScenarioService(new JdbcScenarioRepository(source),scenario,"admin-ficticio",authentication);
        return new Fixture(source,authentication,new RemessaService(authentication,new JdbcRemessaRepository(source),new JdbcTransactions(source),clock,inspection));
    }
    ReceivedContent content() { return new ReceivedContent(RemessaHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8),1); }
    @Test void reaberturaPreservaBytesOrdemHashEIdentificadores() {
        var f=fixture("cenario-a"); String token=f.token("0001"),key=f.service.open(token,202601,"NAO").key();
        var first=f.service.receive(token,key,content());var second=f.service.receive(token,key,content());
        assertNotEquals(first.number(),second.number()); assertEquals(1,first.order());assertEquals(2,second.order());assertEquals(first.hash(),second.hash());
        var reopened=fixture("cenario-a");var state=reopened.service.inspect("admin-ficticio",first.number());
        assertArrayEquals(content().bytes(),state.lote().bytes());assertEquals(1,state.lote().records());assertEquals("0001",state.lote().ug());
        assertEquals(first.number(),state.lote().number()); assertEquals(first.key(),state.lote().key()); assertEquals(first.receivedAt(),state.lote().receivedAt());
        assertEquals(first.number(),reopened.service.consult(token,first.number()).number());
        assertEquals(List.of("ENVIO_ABERTO","LOTE_RECEBIDO","LOTE_RECEBIDO"),state.events().stream().map(RemessaEvent::type).toList());
    }
    @Test void outroUgECenarioValidosNaoAcessamChaveOuLote() {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.service.open(token,202601,"NAO").key();var lote=a.service.receive(token,key,content());
        String otherUg=a.token("0002");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.service.receive(otherUg,key,content())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.service.consult(otherUg,lote.number())).kind());
        var b=fixture("cenario-b");String otherScenario=b.token("0001");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.service.receive(otherScenario,key,content())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.service.consult(otherScenario,lote.number())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.service.inspect("admin-ficticio",lote.number())).kind());
        assertEquals(2,a.service.inspect("admin-ficticio",lote.number()).events().size());
    }
    @Test void falhaDeStatementReverteLoteEventoEAtividade() {
        var f=fixture("cenario-a");String token=f.token("0001");var remessa=f.service.open(token,202601,"NAO");var jdbc=new JdbcTemplate(f.source);
        jdbc.execute("CREATE TRIGGER impedir_evento BEFORE INSERT ON remessa_eventos WHEN NEW.tipo='LOTE_RECEBIDO' BEGIN SELECT RAISE(ABORT,'falha ficticia'); END");
        assertThrows(org.springframework.dao.DataAccessException.class,()->f.service.receive(token,remessa.key(),content()));
        assertTrue(new JdbcRemessaRepository(f.source).inspectLote(1,"cenario-a").isEmpty());
        assertEquals(1,new JdbcRemessaRepository(f.source).events(remessa.id()).size());
        assertEquals(INITIAL,new JdbcRemessaRepository(f.source).find(remessa.key(),"cenario-a","0001").orElseThrow().lastActivity());
    }
    @Test void falhaRealDeCommitReverteTudoAntesDaConfirmacao() {
        var f=fixture("cenario-a");String token=f.token("0001");var remessa=f.service.open(token,202601,"NAO");var jdbc=new JdbcTemplate(f.source);
        jdbc.execute("CREATE TABLE falha_commit(remessa_id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER adiar_falha AFTER INSERT ON remessa_eventos WHEN NEW.tipo='LOTE_RECEBIDO' BEGIN INSERT INTO falha_commit VALUES (-1); END");
        assertThrows(org.springframework.transaction.TransactionSystemException.class,()->f.service.receive(token,remessa.key(),content()));
        assertTrue(new JdbcRemessaRepository(f.source).inspectLote(1,"cenario-a").isEmpty());
        assertEquals(1,new JdbcRemessaRepository(f.source).events(remessa.id()).size());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM falha_commit",Integer.class));
        jdbc.execute("DROP TRIGGER adiar_falha");
        assertEquals(1,f.service.receive(token,remessa.key(),content()).order());
    }
    @Test void concorrenciaAbreUmaRemessaERecebeTodosOsLotesSemDeduplicar() throws Exception {
        var f=fixture("cenario-a");String token=f.token("0001");var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(4)) {
            var results=new ArrayList<Future<Object>>();
            for(int i=0;i<4;i++) results.add(pool.submit(()->{ start.await();try{return f.service.open(token,202601,"NAO");}catch(RemessaFailure e){return e.kind();} }));
            start.countDown();var opened=new ArrayList<Remessa>();int conflicts=0;
            for(var result:results) { Object value=result.get(10,TimeUnit.SECONDS);if(value instanceof Remessa r)opened.add(r);else {assertEquals(RemessaFailure.Kind.CONFLICT,value);conflicts++;} }
            assertEquals(1,opened.size());assertEquals(3,conflicts);
            String key=opened.getFirst().key();var receipts=new ArrayList<Future<Lote>>();
            for(int i=0;i<4;i++) receipts.add(pool.submit(()->f.service.receive(token,key,content())));
            var orders=new TreeSet<Integer>();var numbers=new HashSet<Long>();
            for(var result:receipts){var lote=result.get(10,TimeUnit.SECONDS);orders.add(lote.order());numbers.add(lote.number());}
            assertEquals(Set.of(1,2,3,4),orders);assertEquals(4,numbers.size());
            assertEquals(5,new JdbcRemessaRepository(f.source).events(opened.getFirst().id()).size());
        }
    }
    @Test void contencaoRealNaoConfirmaAberturaParcial() throws Exception {
        var f=fixture("cenario-a");String token=f.token("0001");
        var config=new SQLiteConfig();config.enforceForeignKeys(true);config.setBusyTimeout(1);config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
        var competing=new SQLiteDataSource(config);competing.setUrl("jdbc:sqlite:"+settings("cenario-a").database);
        var other=fixture(competing,"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC));
        try(var connection=f.source.getConnection()) {
            connection.setAutoCommit(false);
            assertThrows(org.springframework.transaction.CannotCreateTransactionException.class,()->other.service.open(token,202601,"NAO"));
            connection.rollback();
        }
        assertEquals(0,new JdbcTemplate(f.source).queryForObject("SELECT COUNT(*) FROM remessas",Integer.class));
        assertNotNull(other.service.open(token,202601,"NAO"));
    }
    @Test void fksRejeitamVinculosCruzadosDeLoteEEvento() {
        var f=fixture("cenario-a");String token=f.token("0001");var r=f.service.open(token,202601,"NAO");var lote=f.service.receive(token,r.key(),content());var jdbc=new JdbcTemplate(f.source);
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE lotes SET ug='0002' WHERE numero=?",lote.number()));
        var other=f.service.open(f.token("0002"),202601,"NAO");
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",other.id(),"cenario-a","0002",lote.number(),"FICTICIO",INITIAL.toString()));
        assertEquals("0001",f.service.inspect("admin-ficticio",lote.number()).lote().ug());
    }
    @Test void tokenExpiradoEParametrosInvalidosNaoGeramEfeitos() {
        var f=fixture("cenario-a");String token=f.token("0001");
        for(int month:new int[]{202512,202600,202613,202701}) assertThrows(RemessaFailure.class,()->f.service.open(token,month,"NAO"));
        var expired=fixture(f.source,"cenario-a",Clock.fixed(INITIAL.plusSeconds(3600),ZoneOffset.UTC));
        assertThrows(AuthenticationFailure.class,()->expired.service.open(token,202601,"NAO"));
        assertThrows(AuthenticationFailure.class,()->f.service.open("invalido",202601,"NAO"));
        assertEquals(0,new JdbcTemplate(f.source).queryForObject("SELECT COUNT(*) FROM remessas",Integer.class));
    }
    @Test void migracaoV1PreservaSessaoEVerificaChecksumAntesDeV2() throws Exception {
        var raw=new SQLiteDataSource();raw.setUrl("jdbc:sqlite:"+settings("cenario-a").database);
        var v1=new ClassPathResource("db/migration/V1__authentication.sql");
        try(var c=raw.getConnection()){ScriptUtils.executeSqlScript(c,v1);}
        var jdbc=new JdbcTemplate(raw);jdbc.update("INSERT INTO schema_migrations VALUES(1,?)",CredentialHash.token(v1.getContentAsString(StandardCharsets.UTF_8)));
        jdbc.update("INSERT INTO scenarios(id,profile,seed,initial_time,clock_mode,token_seconds) VALUES('cenario-a','2026/V6/6.06',1,?,'fixed',3600)",INITIAL.toString());
        String salt=CredentialHash.salt();jdbc.update("INSERT INTO users VALUES(?,?,?,?,?)","cenario-a","usuario-ficticio","acesso-ficticio",CredentialHash.password("senha-ficticia",salt),salt);
        jdbc.update("INSERT INTO user_ugs VALUES('cenario-a','usuario-ficticio','0001')");jdbc.update("INSERT INTO user_ugs VALUES('cenario-a','usuario-ficticio','0002')");
        var original=new Session("usuario-ficticio","0001","cenario-a",INITIAL,INITIAL.plusSeconds(3600));new JdbcAuthenticationRepository(raw).saveSession("preservado",original);
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado'");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name='remessas'",Integer.class));
        jdbc.update("UPDATE schema_migrations SET checksum=?",CredentialHash.token(v1.getContentAsString(StandardCharsets.UTF_8)));
        var migrated=SqliteDatabase.open(settings("cenario-a"));assertEquals(original,new JdbcAuthenticationRepository(migrated).findSession("preservado","cenario-a").orElseThrow());
        assertEquals(2,new JdbcTemplate(migrated).queryForObject("PRAGMA user_version",Integer.class));
        assertNotNull(fixture("cenario-a").service.open("preservado",202601,"NAO"));
    }
}
