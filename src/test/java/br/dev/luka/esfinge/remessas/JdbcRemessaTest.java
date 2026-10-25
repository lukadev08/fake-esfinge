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
    ReceivedContent content() { return new ReceivedContent(RemessaHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8),1,Assunto.CADASTRO_CONTRIBUINTE,List.of()); }
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
        assertEquals(5,new JdbcTemplate(migrated).queryForObject("PRAGMA user_version",Integer.class));
        assertNotNull(fixture("cenario-a").service.open("preservado",202601,"NAO"));
    }
    ReceivedContent creditContent() {
        return new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(CreditoHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }
    @Test void creditoTextualExatoReabreSemDeduplicarEVinculaValoresAoLote() throws Exception {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.service.open(token,202601,"NAO").key();
        var first=f.service.receive(token,key,creditContent());var second=f.service.receive(token,key,creditContent());
        var jdbc=new JdbcTemplate(f.source);assertNotEquals(first.number(),second.number());
        assertEquals(List.of("text","text"),jdbc.queryForList("SELECT typeof(valor) FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(List.of("123456789012345678901234567890.12","123456789012345678901234567890.12"),jdbc.queryForList("SELECT valor FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(List.of(0,0),jdbc.queryForList("SELECT indice FROM valores_recebidos ORDER BY lote_id",Integer.class));
        var reopened=fixture("cenario-a");var state=reopened.service.inspect("admin-ficticio",first.number());
        assertArrayEquals(creditContent().bytes(),state.lote().bytes());assertEquals(Assunto.LANCAMENTO_CREDITO_TRIBUTARIO,state.lote().subject());
        assertEquals(first.number(),reopened.service.consult(token,first.number()).number());assertEquals(2,second.order());
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE valores_recebidos SET ug='0002' WHERE lote_id=?",first.number()));
        var other=fixture("cenario-b");String otherToken=other.token("0001");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.receive(otherToken,key,creditContent())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.inspect("admin-ficticio",first.number())).kind());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        for(String spelling:List.of("1","1.0","1.00")) {
            byte[] original=CreditoHttpTest.PAYLOAD.replace("123456789012345678901234567890.12",spelling).getBytes(StandardCharsets.UTF_8);
            var received=new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(original);
            var lote=reopened.service.receive(token,key,received);
            assertEquals(0,new java.math.BigDecimal(jdbc.queryForObject("SELECT valor FROM valores_recebidos WHERE lote_id=?",String.class,lote.number())).compareTo(java.math.BigDecimal.ONE));
            assertArrayEquals(original,fixture("cenario-a").service.inspect("admin-ficticio",lote.number()).lote().bytes());
        }
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.LANCAMENTO_CREDITO_TRIBUTARIO,List.of(new java.math.BigDecimal("1.001"))));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},2,Assunto.LANCAMENTO_CREDITO_TRIBUTARIO,List.of(java.math.BigDecimal.ONE)));
    }
    DataSource versionTwo() throws Exception {
        var config=new SQLiteConfig();config.enforceForeignKeys(true);var raw=new SQLiteDataSource(config);raw.setUrl("jdbc:sqlite:"+settings("cenario-a").database);
        for(int version=1;version<=2;version++) {
            var resource=new ClassPathResource("db/migration/"+(version==1?"V1__authentication.sql":"V2__remessas.sql"));
            try(var c=raw.getConnection()){ScriptUtils.executeSqlScript(c,resource);}
            new JdbcTemplate(raw).update("INSERT INTO schema_migrations VALUES(?,?)",version,CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        }
        var jdbc=new JdbcTemplate(raw);jdbc.update("INSERT INTO scenarios(id,profile,seed,initial_time,clock_mode,token_seconds) VALUES('cenario-a','2026/V6/6.06',1,?,'fixed',3600)",INITIAL.toString());
        String salt=CredentialHash.salt();jdbc.update("INSERT INTO users VALUES(?,?,?,?,?)","cenario-a","usuario-ficticio","acesso-ficticio",CredentialHash.password("senha-ficticia",salt),salt);
        jdbc.update("INSERT INTO user_ugs VALUES('cenario-a','usuario-ficticio','0001')");jdbc.update("INSERT INTO user_ugs VALUES('cenario-a','usuario-ficticio','0002')");
        return raw;
    }
    @Test void migracaoV2ConservaLotesEventosSessaoFksEHighWater() throws Exception {
        var raw=versionTwo();var old=fixture(raw,"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC));String token=old.token("0001"),key=old.service.open(token,202601,"NAO").key();
        var lote=old.service.receive(token,key,content());old.service.consult(token,lote.number());var events=old.service.inspect("admin-ficticio",lote.number()).events();
        var jdbc=new JdbcTemplate(raw);jdbc.update("UPDATE sqlite_sequence SET seq=42 WHERE name='lotes'");jdbc.update("UPDATE sqlite_sequence SET seq=77 WHERE name='remessa_eventos'");
        String checksum=jdbc.queryForObject("SELECT checksum FROM schema_migrations WHERE version=2",String.class);
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=2");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(2,jdbc.queryForObject("PRAGMA user_version",Integer.class));jdbc.update("UPDATE schema_migrations SET checksum=? WHERE version=2",checksum);
        var migrated=fixture("cenario-a");var state=migrated.service.inspect("admin-ficticio",lote.number());
        assertEquals(events,state.events());assertArrayEquals(content().bytes(),state.lote().bytes());assertEquals(lote.number(),state.lote().number());assertEquals(lote.hash(),state.lote().hash());
        assertEquals(5,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(5,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        var credit=migrated.service.receive(token,key,creditContent());assertEquals(43,credit.number());assertEquals(2,credit.order());
        assertEquals(78,migrated.service.inspect("admin-ficticio",credit.number()).events().getLast().id());
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE lotes SET ug='0002' WHERE numero=?",lote.number()));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
    }
    @Test void falhaIntermediariaDeMigracaoV3ReverteEstruturaEDados() throws Exception {
        var raw=versionTwo();var old=fixture(raw,"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC));String token=old.token("0001"),key=old.service.open(token,202601,"NAO").key();var lote=old.service.receive(token,key,content());
        var jdbc=new JdbcTemplate(raw);jdbc.execute("CREATE VIEW remessa_eventos_v3 AS SELECT 1 AS conflito");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(2,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name IN ('lotes_v3','sequencias_v3','valores_creditos','valores_recebidos')",Integer.class));
        assertArrayEquals(content().bytes(),jdbc.queryForObject("SELECT payload FROM lotes WHERE numero=?",byte[].class,lote.number()));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos",Integer.class));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.execute("DROP VIEW remessa_eventos_v3");assertNotNull(fixture("cenario-a").service.receive(token,key,creditContent()));
    }

    ReceivedContent collectionContent() {
        return new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(ArrecadacaoHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }
    @Test void arrecadacaoTextualReabrePreservaMultiplosLotesEVinculos() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.service.open(token,202601,"NAO").key();
        var first=f.service.receive(token,key,collectionContent());var second=f.service.receive(token,key,collectionContent());
        var jdbc=new JdbcTemplate(f.source);assertNotEquals(first.number(),second.number());assertEquals(2,second.order());
        assertEquals(List.of("text","text"),jdbc.queryForList("SELECT typeof(valor) FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(List.of("123456789012345678901234567890.12","123456789012345678901234567890.12"),jdbc.queryForList("SELECT valor FROM valores_recebidos ORDER BY lote_id",String.class));
        var reopened=fixture("cenario-a");var state=reopened.service.inspect("admin-ficticio",first.number());
        assertArrayEquals(collectionContent().bytes(),state.lote().bytes());assertEquals(Assunto.DIARIO_ARRECADACAO,state.lote().subject());
        assertEquals(first.number(),reopened.service.consult(token,first.number()).number());
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE valores_recebidos SET ug='0002' WHERE lote_id=?",first.number()));
        var other=fixture("cenario-b");String otherToken=other.token("0001");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.receive(otherToken,key,collectionContent())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.inspect("admin-ficticio",first.number())).kind());
        for(String spelling:List.of("1","1.0","1.00")) {
            byte[] original=ArrecadacaoHttpTest.PAYLOAD.replace("123456789012345678901234567890.12",spelling).getBytes(StandardCharsets.UTF_8);
            var received=new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(original);
            var lote=reopened.service.receive(token,key,received);
            assertEquals(0,new java.math.BigDecimal(jdbc.queryForObject("SELECT valor FROM valores_recebidos WHERE lote_id=?",String.class,lote.number())).compareTo(java.math.BigDecimal.ONE));
            assertArrayEquals(original,fixture("cenario-a").service.inspect("admin-ficticio",lote.number()).lote().bytes());
        }
        var credit=reopened.service.receive(token,key,creditContent());assertEquals(6,credit.order());
        assertEquals(6,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.DIARIO_ARRECADACAO,List.of(new java.math.BigDecimal("1.001"))));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},2,Assunto.DIARIO_ARRECADACAO,List.of(java.math.BigDecimal.ONE)));
    }
    record Legacy(DataSource source,String token,String key,Lote contributor,Lote credit,List<RemessaEvent> events) {}
    Legacy versionThreePopulated() throws Exception {
        var raw=versionTwo();var resource=new ClassPathResource("db/migration/V3__creditos.sql");
        try(var c=raw.getConnection()){ScriptUtils.executeSqlScript(c,resource);}
        var jdbc=new JdbcTemplate(raw);jdbc.update("INSERT INTO schema_migrations VALUES(3,?)",CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        var old=fixture(raw,"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC));String token=old.token("0001");var remessa=old.service.open(token,202601,"NAO");
        var contributor=old.service.receive(token,remessa.key(),content());
        long credit=jdbc.queryForObject("INSERT INTO lotes(remessa_id,cenario,ug,ordem,assunto,payload,hash,registros,recebimento) VALUES (?,?,?,?,?,?,?,?,?) RETURNING numero",Long.class,
                remessa.id(),"cenario-a","0001",2,"LANCAMENTO_CREDITO_TRIBUTARIO",creditContent().bytes(),CredentialHash.token(CreditoHttpTest.PAYLOAD),1,INITIAL.toString());
        jdbc.update("INSERT INTO valores_creditos VALUES(?,?,?,?,?,?)",credit,remessa.id(),"cenario-a","0001",0,"123456789012345678901234567890.12");
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa.id(),"cenario-a","0001",credit,"LOTE_RECEBIDO",INITIAL.toString());
        var inspected=old.service.inspect("admin-ficticio",credit);
        return new Legacy(raw,token,remessa.key(),contributor,inspected.lote(),inspected.events());
    }
    @Test void migracaoV3PreservaDinheiroContribuinteCreditoSessaoEFks() throws Exception {
        var old=versionThreePopulated();var jdbc=new JdbcTemplate(old.source());
        jdbc.update("UPDATE sqlite_sequence SET seq=42 WHERE name='lotes'");jdbc.update("UPDATE sqlite_sequence SET seq=77 WHERE name='remessa_eventos'");
        String checksum=jdbc.queryForObject("SELECT checksum FROM schema_migrations WHERE version=3",String.class);
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=3");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(3,jdbc.queryForObject("PRAGMA user_version",Integer.class));jdbc.update("UPDATE schema_migrations SET checksum=? WHERE version=3",checksum);
        var migrated=fixture("cenario-a");var state=migrated.service.inspect("admin-ficticio",old.credit().number());
        assertEquals(old.events(),state.events());assertArrayEquals(creditContent().bytes(),state.lote().bytes());assertEquals(old.credit().hash(),state.lote().hash());
        assertArrayEquals(content().bytes(),migrated.service.inspect("admin-ficticio",old.contributor().number()).lote().bytes());
        assertEquals("123456789012345678901234567890.12",jdbc.queryForObject("SELECT valor FROM valores_recebidos WHERE lote_id=?",String.class,old.credit().number()));
        assertEquals("text",jdbc.queryForObject("SELECT typeof(valor) FROM valores_recebidos",String.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name='valores_creditos'",Integer.class));
        assertEquals(5,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(5,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        var collection=migrated.service.receive(old.token(),old.key(),collectionContent());assertEquals(43,collection.number());assertEquals(3,collection.order());
        assertEquals(78,migrated.service.inspect("admin-ficticio",collection.number()).events().getLast().id());
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE valores_recebidos SET ug='0002' WHERE lote_id=?",collection.number()));
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE lotes SET ug='0002' WHERE numero=?",old.credit().number()));
    }
    @Test void falhaV4ReverteInclusiveValoresMonetariosDaV3() throws Exception {
        var old=versionThreePopulated();var jdbc=new JdbcTemplate(old.source());
        jdbc.execute("CREATE VIEW valores_recebidos_v4 AS SELECT 1 AS conflito");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(3,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name IN ('lotes_v4','remessa_eventos_v4','sequencias_v4','valores_recebidos')",Integer.class));
        assertEquals("123456789012345678901234567890.12",jdbc.queryForObject("SELECT valor FROM valores_creditos WHERE lote_id=?",String.class,old.credit().number()));
        assertArrayEquals(creditContent().bytes(),jdbc.queryForObject("SELECT payload FROM lotes WHERE numero=?",byte[].class,old.credit().number()));
        assertEquals(old.events(),fixture(old.source(),"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC)).service.inspect("admin-ficticio",old.credit().number()).events());
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.execute("DROP VIEW valores_recebidos_v4");assertNotNull(fixture("cenario-a").service.receive(old.token(),old.key(),collectionContent()));
    }
    ReceivedContent writeOffContent() {
        return new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(BaixaHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8));
    }
    @Test void baixaTextualReabrePreservaMultiplosLotesEVinculos() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.service.open(token,202601,"NAO").key();
        var first=f.service.receive(token,key,writeOffContent());var second=f.service.receive(token,key,writeOffContent());
        var jdbc=new JdbcTemplate(f.source);assertNotEquals(first.number(),second.number());assertEquals(2,second.order());
        assertEquals(List.of("text","text"),jdbc.queryForList("SELECT typeof(valor) FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(List.of("123456789012345678901234567890.12","123456789012345678901234567890.12"),jdbc.queryForList("SELECT valor FROM valores_recebidos ORDER BY lote_id",String.class));
        var reopened=fixture("cenario-a");var state=reopened.service.inspect("admin-ficticio",first.number());
        assertArrayEquals(writeOffContent().bytes(),state.lote().bytes());assertEquals(Assunto.BAIXA_CREDITO_TRIBUTARIO,state.lote().subject());
        assertEquals(first.number(),reopened.service.consult(token,first.number()).number());
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE valores_recebidos SET ug='0002' WHERE lote_id=?",first.number()));
        var other=fixture("cenario-b");String otherToken=other.token("0001");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.receive(otherToken,key,writeOffContent())).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->other.service.inspect("admin-ficticio",first.number())).kind());
        for(String spelling:List.of("1","1.0","1.00")) {
            byte[] original=BaixaHttpTest.PAYLOAD.replace("123456789012345678901234567890.12",spelling).getBytes(StandardCharsets.UTF_8);
            var received=new br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload().read(original);
            var lote=reopened.service.receive(token,key,received);
            assertEquals(0,new java.math.BigDecimal(jdbc.queryForObject("SELECT valor FROM valores_recebidos WHERE lote_id=?",String.class,lote.number())).compareTo(java.math.BigDecimal.ONE));
            assertArrayEquals(original,fixture("cenario-a").service.inspect("admin-ficticio",lote.number()).lote().bytes());
        }
        assertEquals(6,reopened.service.receive(token,key,creditContent()).order());assertEquals(7,reopened.service.receive(token,key,collectionContent()).order());
        assertEquals(7,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.BAIXA_CREDITO_TRIBUTARIO,List.of(new java.math.BigDecimal("1.001"))));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},2,Assunto.BAIXA_CREDITO_TRIBUTARIO,List.of(java.math.BigDecimal.ONE)));
    }
    record LegacyFour(Legacy old,Lote collection,List<RemessaEvent> events) {}
    LegacyFour versionFourPopulated() throws Exception {
        var old=versionThreePopulated();var resource=new ClassPathResource("db/migration/V4__arrecadacao.sql");
        try(var c=old.source().getConnection()){ScriptUtils.executeSqlScript(c,resource);}
        var jdbc=new JdbcTemplate(old.source());jdbc.update("INSERT INTO schema_migrations VALUES(4,?)",CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        var service=fixture(old.source(),"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC)).service;
        var collection=service.receive(old.token(),old.key(),collectionContent());
        return new LegacyFour(old,collection,service.inspect("admin-ficticio",collection.number()).events());
    }
    @Test void migracaoV4PreservaCreditoArrecadacaoSessaoEFks() throws Exception {
        var old=versionFourPopulated();var jdbc=new JdbcTemplate(old.old().source());
        jdbc.update("UPDATE sqlite_sequence SET seq=42 WHERE name='lotes'");jdbc.update("UPDATE sqlite_sequence SET seq=77 WHERE name='remessa_eventos'");
        String checksum=jdbc.queryForObject("SELECT checksum FROM schema_migrations WHERE version=4",String.class);
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=4");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(4,jdbc.queryForObject("PRAGMA user_version",Integer.class));jdbc.update("UPDATE schema_migrations SET checksum=? WHERE version=4",checksum);
        var migrated=fixture("cenario-a");var state=migrated.service.inspect("admin-ficticio",old.collection().number());
        assertEquals(old.events(),state.events());
        for(var lote:List.of(old.old().contributor(),old.old().credit(),old.collection())) {
            var preserved=migrated.service.inspect("admin-ficticio",lote.number()).lote();
            assertEquals(lote.number(),preserved.number());assertEquals(lote.hash(),preserved.hash());assertArrayEquals(lote.bytes(),preserved.bytes());
        }
        assertEquals(List.of("123456789012345678901234567890.12","123456789012345678901234567890.12"),jdbc.queryForList("SELECT valor FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(List.of("text","text"),jdbc.queryForList("SELECT typeof(valor) FROM valores_recebidos ORDER BY lote_id",String.class));
        assertEquals(5,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(5,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        var baixa=migrated.service.receive(old.old().token(),old.old().key(),writeOffContent());assertEquals(43,baixa.number());assertEquals(4,baixa.order());
        assertEquals(78,migrated.service.inspect("admin-ficticio",baixa.number()).events().getLast().id());
        assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE valores_recebidos SET ug='0002' WHERE lote_id=?",baixa.number()));
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE lotes SET ug='0002' WHERE numero=?",old.old().credit().number()));
    }
    @Test void falhaV5ReverteInclusiveDinheiroDeCreditoEArrecadacao() throws Exception {
        var old=versionFourPopulated();var jdbc=new JdbcTemplate(old.old().source());
        jdbc.execute("CREATE VIEW valores_recebidos_v5 AS SELECT 1 AS conflito");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(4,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name IN ('lotes_v5','remessa_eventos_v5','sequencias_v5')",Integer.class));
        assertEquals(List.of("123456789012345678901234567890.12","123456789012345678901234567890.12"),jdbc.queryForList("SELECT valor FROM valores_recebidos ORDER BY lote_id",String.class));
        for(var lote:List.of(old.old().contributor(),old.old().credit(),old.collection()))
            assertArrayEquals(lote.bytes(),jdbc.queryForObject("SELECT payload FROM lotes WHERE numero=?",byte[].class,lote.number()));
        assertEquals(old.events(),fixture(old.old().source(),"cenario-a",Clock.fixed(INITIAL,ZoneOffset.UTC)).service.inspect("admin-ficticio",old.collection().number()).events());
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.execute("DROP VIEW valores_recebidos_v5");assertNotNull(fixture("cenario-a").service.receive(old.old().token(),old.old().key(),writeOffContent()));
    }
}
