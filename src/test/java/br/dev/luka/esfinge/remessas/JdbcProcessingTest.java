package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.*;
import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.configuration.*;
import br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.application.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.sqlite.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class JdbcProcessingTest {
    @TempDir Path temporary;
    static final Instant INITIAL=Instant.parse("2026-01-01T00:00:00Z");
    static final String ADMIN="admin-ficticio";
    SimulatorSettings settings(String scenario) {return new SimulatorSettings(temporary.resolve("state.sqlite").toString(),scenario,"2026/V6/6.06",ADMIN,"fixed",1,INITIAL,Duration.ofHours(1),List.of(new SimulatorSettings.User("usuario-ficticio","acesso-ficticio","senha-ficticia",Set.of("0001","0002"))));}
    record Fixture(DataSource source,AuthenticationService auth,RemessaService remessas,ProcessingService processing,JdbcBusinessClock clock) {
        String token(String ug) {return auth.login("acesso-ficticio","senha-ficticia",ug);}
        Lote receive(String token,String key,String payload) {return remessas.receive(token,key,new LotePayload().read(payload.getBytes(StandardCharsets.UTF_8)));}
    }
    Fixture fixture(String scenario) {
        var source=SqliteDatabase.open(settings(scenario));var clock=new JdbcBusinessClock(source,scenario,Clock.fixed(INITIAL,ZoneOffset.UTC));
        var auth=new AuthenticationService(new JdbcAuthenticationRepository(source),clock,scenario,Duration.ofHours(1));
        var inspection=new ScenarioService(new JdbcScenarioRepository(source),scenario,ADMIN,auth);var repository=new JdbcRemessaRepository(source);var tx=new JdbcTransactions(source);
        var remessas=new RemessaService(auth,repository,tx,clock,inspection);
        return new Fixture(source,auth,remessas,new ProcessingService(auth,inspection,remessas,repository,new JdbcProcessingRepository(source),tx,clock,clock,new StructuralRecords(),scenario),clock);
    }
    @Test void horizonteDeConclusaoRejeitaSnapshotInteiroSemEfeitos() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();
        var a=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.receive(token,key,PrazoHttpTest.PAYLOAD);
        long latency=Duration.between(INITIAL,Instant.parse("9999-12-31T23:59:59Z")).toMillis();
        assertDoesNotThrow(()->f.clock.validate(INITIAL.plusMillis(latency)));
        var jdbc=new JdbcTemplate(f.source);var before=jdbc.queryForList("SELECT * FROM remessas");var events=jdbc.queryForList("SELECT * FROM remessa_eventos");
        for(long candidate:List.of(Long.MAX_VALUE,latency)) {
            f.processing.configure(ADMIN,Processing.Mode.MANUAL,candidate);
            assertEquals(RemessaFailure.Kind.INVALID_REQUEST,assertThrows(RemessaFailure.class,()->f.processing.start(token,key)).kind());
        }
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos",Integer.class));
        assertEquals(before,jdbc.queryForList("SELECT * FROM remessas"));assertEquals(events,jdbc.queryForList("SELECT * FROM remessa_eventos"));
        assertEquals(INITIAL,f.clock.instant());assertEquals(latency,f.processing.configuration(ADMIN).latenciaMilissegundos());
        f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);f.processing.start(token,key);assertEquals(4,f.processing.advance(ADMIN,0,4).passosExecutados());
        assertEquals(Processing.State.PROCESSADO_SUCESSO,f.processing.inspect(ADMIN,a.number()).state());
    }
    @Test void avancoExtremoNaoPersisteTempoOuExecutaTrabalho() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();
        var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing.start(token,key);
        var jdbc=new JdbcTemplate(f.source);var jobs=jdbc.queryForList("SELECT * FROM processamento_trabalhos");var events=jdbc.queryForList("SELECT * FROM remessa_eventos");
        assertEquals(RemessaFailure.Kind.INVALID_REQUEST,assertThrows(RemessaFailure.class,()->f.processing.advance(ADMIN,Long.MAX_VALUE,10)).kind());
        assertEquals(INITIAL,f.clock.instant());assertEquals(jobs,jdbc.queryForList("SELECT * FROM processamento_trabalhos"));assertEquals(events,jdbc.queryForList("SELECT * FROM remessa_eventos"));
        assertTrue(f.processing.inspect(ADMIN,lot.number()).effective().isEmpty());assertEquals(INITIAL,fixture("cenario-a").clock.instant());
    }
    @Test void tempoRealNaoUltrapassaHorizonteContratualNemAposReabertura() {
        var f=fixture("cenario-a");var current=new java.util.concurrent.atomic.AtomicReference<>(INITIAL);
        Clock base=new Clock() {
            public ZoneId getZone() {return ZoneOffset.UTC;}
            public Clock withZone(ZoneId zone) {return this;}
            public Instant instant() {return current.get();}
        };
        var clock=new JdbcBusinessClock(f.source,"cenario-a",base);var last=Instant.parse("9999-12-31T23:59:59.999Z");
        clock.advance(Duration.between(INITIAL,last).toMillis());assertEquals(last,clock.instant());
        var jdbc=new JdbcTemplate(f.source);var before=jdbc.queryForList("SELECT * FROM relogio_negocio");
        current.set(INITIAL.plusMillis(2));
        assertEquals(RemessaFailure.Kind.INVALID_REQUEST,assertThrows(RemessaFailure.class,clock::instant).kind());
        var restarted=new JdbcBusinessClock(SqliteDatabase.open(settings("cenario-a")),"cenario-a",base);
        assertEquals(RemessaFailure.Kind.INVALID_REQUEST,assertThrows(RemessaFailure.class,restarted::instant).kind());
        assertEquals(before,jdbc.queryForList("SELECT * FROM relogio_negocio"));
    }
    @Test void reaberturaConservaConfiguracaoTempoEtapasEConteudoSemReaplicar() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();
        f.processing.configure(ADMIN,Processing.Mode.MANUAL,17);var lot=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing.start(token,key);f.processing.advance(ADMIN,0,1);
        f.processing.configure(ADMIN,Processing.Mode.AUTOMATICO,0);
        var restarted=fixture("cenario-a");assertEquals(Processing.Mode.AUTOMATICO,restarted.processing.configuration(ADMIN).modo());assertEquals(0,restarted.processing.configuration(ADMIN).latenciaMilissegundos());
        assertEquals(Processing.State.EM_PROCESSAMENTO,restarted.processing.consult(token,lot.number()).state());
        assertEquals(0,restarted.processing.advance(ADMIN,16,1).passosExecutados());
        restarted=fixture("cenario-a");assertEquals(INITIAL.plusMillis(16),restarted.clock.instant());assertEquals(1,restarted.processing.advance(ADMIN,1,1).passosExecutados());
        var done=fixture("cenario-a");var snapshot=done.processing.inspect(ADMIN,lot.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,snapshot.state());
        assertEquals(1,snapshot.processed().size());assertEquals(1,snapshot.effective().size());assertArrayEquals(lot.bytes(),snapshot.received().lote().bytes());
        assertArrayEquals(snapshot.processed().getFirst(),snapshot.effective().getFirst());
        var mutable=snapshot.processed();mutable.getFirst()[0]=0;assertNotEquals(0,snapshot.processed().getFirst()[0]);
        assertEquals(0,done.processing.advance(ADMIN,1000,100).passosExecutados());assertEquals(1,done.processing.inspect(ADMIN,lot.number()).effective().size());
        assertTrue(new JdbcTemplate(done.source).queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void ordemPersistidaDesempataInstantesEConsultaNaoConsomeTrabalho() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);
        var a=f.receive(token,key,RemessaHttpTest.PAYLOAD);var b=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing.start(token,key);
        f.processing.advance(ADMIN,0,1);assertEquals(Processing.State.EM_PROCESSAMENTO,f.processing.consult(token,a.number()).state());assertEquals(Processing.State.RECEBIDO,f.processing.consult(token,b.number()).state());
        f.processing.advance(ADMIN,0,1);assertEquals(Processing.State.PROCESSADO_SUCESSO,f.processing.consult(token,a.number()).state());assertEquals(Processing.State.RECEBIDO,f.processing.consult(token,b.number()).state());
        f.processing.advance(ADMIN,0,1);assertEquals(Processing.State.EM_PROCESSAMENTO,f.processing.consult(token,b.number()).state());f.processing.advance(ADMIN,0,1);
        assertEquals(Processing.State.PROCESSADO_SUCESSO,f.processing.inspect(ADMIN,b.number()).state());
    }
    @Test void relogioCompartilhadoExpiraTokenNoLimiteSemPerderLote() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);
        f.processing.advance(ADMIN,3599999,1);assertNotNull(f.auth.authenticate(token));f.processing.advance(ADMIN,1,1);
        assertThrows(AuthenticationFailure.class,()->f.processing.consult(token,lot.number()));
        var restarted=fixture("cenario-a");String renewed=restarted.token("0001");assertEquals(Processing.State.RECEBIDO,restarted.processing.consult(renewed,lot.number()).state());
        assertEquals(INITIAL.plusSeconds(3600),restarted.clock.instant());
    }
    @Test void concorrenciaEntreInstanciasManualAutomaticoAplicaCadaIndiceUmaVez() throws Exception {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();a.processing.configure(ADMIN,Processing.Mode.AUTOMATICO,0);
        var ids=new ArrayList<Long>();for(int i=0;i<20;i++) ids.add(a.receive(token,key,PrazoHttpTest.PAYLOAD).number());a.processing.start(token,key);
        var b=fixture("cenario-a");var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(4)) {
            var calls=new ArrayList<Future<?>>();
            for(int i=0;i<100;i++) {final int n=i;calls.add(pool.submit(()->{start.await();if(n%2==0)a.processing.advance(ADMIN,1,1);else b.processing.tick();return null;}));}
            start.countDown();for(var call:calls) call.get(30,TimeUnit.SECONDS);
        }
        a.processing.advance(ADMIN,0,100);
        for(long id:ids) {var snapshot=a.processing.inspect(ADMIN,id);assertEquals(Processing.State.PROCESSADO_SUCESSO,snapshot.state());assertEquals(1,snapshot.processed().size());assertEquals(1,snapshot.effective().size());}
        assertEquals(INITIAL.plusMillis(50),a.clock.instant());
        var jdbc=new JdbcTemplate(a.source);assertEquals(20,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));assertEquals(20,jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos WHERE situacao='PROCESSADO_SUCESSO'",Integer.class));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void disparoConcorrenteComRecepcaoMantemSnapshotLinearizavel() throws Exception {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);var first=f.receive(token,key,RemessaHttpTest.PAYLOAD);
        long later;try(var pool=Executors.newFixedThreadPool(2)) {var gate=new CountDownLatch(1);var start=pool.submit(()->{gate.await();return f.processing.start(token,key);});var receive=pool.submit(()->{gate.await();return f.receive(token,key,PrazoHttpTest.PAYLOAD);});gate.countDown();assertNotNull(start.get(30,TimeUnit.SECONDS));later=receive.get(30,TimeUnit.SECONDS).number();}
        f.processing.advance(ADMIN,0,10);assertEquals(Processing.State.PROCESSADO_SUCESSO,f.processing.inspect(ADMIN,first.number()).state());
        if(f.processing.inspect(ADMIN,later).state()==Processing.State.RECEBIDO) {f.processing.start(token,key);f.processing.advance(ADMIN,0,10);}
        assertEquals(1,f.processing.inspect(ADMIN,later).effective().size());assertEquals(1,f.processing.inspect(ADMIN,first.number()).effective().size());
    }
    static java.util.stream.Stream<Arguments> failures() {return java.util.stream.Stream.of("PROCESSAMENTO_DISPARADO","LOTE_EM_PROCESSAMENTO","LOTE_PROCESSADO_SUCESSO").flatMap(type->java.util.stream.Stream.of(Arguments.of(type,false),Arguments.of(type,true)));}
    @ParameterizedTest @MethodSource("failures")
    void falhasReaisDeStatementECommitRevertemCadaTransicao(String event,boolean commit) {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,SaldoHttpTest.PAYLOAD);
        if(!event.equals("PROCESSAMENTO_DISPARADO")) f.processing.start(token,key);
        if(event.equals("LOTE_PROCESSADO_SUCESSO")) f.processing.advance(ADMIN,0,1);
        var before=f.processing.inspect(ADMIN,lot.number());var jdbc=new JdbcTemplate(f.source);var money=jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo");
        if(commit) {jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN INSERT INTO falha_commit VALUES(-1); END");}
        else jdbc.execute("CREATE TRIGGER impedir BEFORE INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN SELECT RAISE(ABORT,'falha ficticia'); END");
        assertThrows(RuntimeException.class,()->{if(event.equals("PROCESSAMENTO_DISPARADO"))f.processing.start(token,key);else f.processing.advance(ADMIN,0,1);});
        var failed=f.processing.inspect(ADMIN,lot.number());assertEquals(before.state(),failed.state());assertEquals(before.received().events(),failed.received().events());
        assertTrue(failed.processed().isEmpty());assertTrue(failed.effective().isEmpty());assertEquals(money,jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo"));
        if(commit) assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM falha_commit",Integer.class));
        jdbc.execute("DROP TRIGGER impedir");
        if(event.equals("PROCESSAMENTO_DISPARADO")) f.processing.start(token,key);f.processing.advance(ADMIN,0,10);
        var result=f.processing.inspect(ADMIN,lot.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,result.state());assertEquals(1,result.effective().size());assertEquals(5,result.received().events().size());
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void geracaoAnteriorNaoProduzEfeitosNaExecucaoAtual() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing.start(token,key);
        new JdbcTemplate(f.source).update("UPDATE scenarios SET generation=1 WHERE id='cenario-a'");
        assertEquals(0,f.processing.advance(ADMIN,1000,10).passosExecutados());assertTrue(f.processing.inspect(ADMIN,lot.number()).effective().isEmpty());
    }
    @Test void outraUgECenarioNaoDisparamConsultamOuInspecionam() {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();var lot=a.receive(token,key,PrazoHttpTest.PAYLOAD);String otherUg=a.token("0002");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.processing.start(otherUg,key)).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.processing.consult(otherUg,lot.number())).kind());
        var b=fixture("cenario-b");String other=b.token("0001");assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.processing.start(other,key)).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.processing.inspect(ADMIN,lot.number())).kind());
    }
    @Test void geracaoAlteradaDuranteValidacaoImpedeCommitDeEfeitos() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();
        f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing.start(token,key);f.processing.advance(ADMIN,0,1);
        var jdbc=new JdbcTemplate(f.source);var before=f.processing.inspect(ADMIN,lot.number());
        var inspection=new ScenarioService(new JdbcScenarioRepository(f.source),"cenario-a",ADMIN,f.auth);
        var guarded=new ProcessingService(f.auth,inspection,f.remessas,new JdbcRemessaRepository(f.source),new JdbcProcessingRepository(f.source),new JdbcTransactions(f.source),f.clock,f.clock,received->{
            var records=new StructuralRecords().process(received);jdbc.update("UPDATE scenarios SET generation=1 WHERE id='cenario-a'");return records;
        },"cenario-a");
        assertEquals(0,guarded.advance(ADMIN,0,1).passosExecutados());
        var after=guarded.inspect(ADMIN,lot.number());assertEquals(before.state(),after.state());assertEquals(before.received().events(),after.received().events());
        assertTrue(after.processed().isEmpty());assertTrue(after.effective().isEmpty());
    }
    record Legacy(DataSource source,String token,String key,List<Lote> lots,List<RemessaEvent> events) {}
    Legacy legacySeven() throws Exception {
        var config=new SQLiteConfig();config.enforceForeignKeys(true);config.setBusyTimeout(5000);config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
        var source=new SQLiteDataSource(config);source.setUrl("jdbc:sqlite:"+settings("cenario-a").database);
        String[] scripts={"V1__authentication.sql","V2__remessas.sql","V3__creditos.sql","V4__arrecadacao.sql","V5__baixas.sql","V6__saldo.sql","V7__prazo.sql"};
        for(int i=0;i<scripts.length;i++) {var resource=new ClassPathResource("db/migration/"+scripts[i]);try(var c=source.getConnection()){ScriptUtils.executeSqlScript(c,resource);}new JdbcTemplate(source).update("INSERT INTO schema_migrations VALUES(?,?)",i+1,CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));}
        var jdbc=new JdbcTemplate(source);var s=settings("cenario-a");
        jdbc.update("INSERT INTO scenarios(id,profile,seed,initial_time,clock_mode,token_seconds) VALUES(?,?,?,?,?,?)",s.scenario,s.profile,s.seed,s.initialTime.toString(),s.clockMode,s.tokenValidity.toSeconds());
        String salt=CredentialHash.salt();jdbc.update("INSERT INTO users VALUES(?,?,?,?,?)",s.scenario,"usuario-ficticio","acesso-ficticio",CredentialHash.password("senha-ficticia",salt),salt);
        for(String ug:List.of("0001","0002")) jdbc.update("INSERT INTO user_ugs VALUES(?,?,?)",s.scenario,"usuario-ficticio",ug);
        var auth=new AuthenticationService(new JdbcAuthenticationRepository(source),Clock.fixed(INITIAL,ZoneOffset.UTC),s.scenario,s.tokenValidity);
        var inspection=new ScenarioService(new JdbcScenarioRepository(source),s.scenario,ADMIN,auth);var repository=new LegacyRemessaRepository(source);
        var remessas=new RemessaService(auth,repository,new JdbcTransactions(source),Clock.fixed(INITIAL,ZoneOffset.UTC),inspection);String token=auth.login("acesso-ficticio","senha-ficticia","0001"),key=remessas.open(token,202601,"NAO").key();
        var lots=new ArrayList<Lote>();for(String payload:List.of(RemessaHttpTest.PAYLOAD,CreditoHttpTest.PAYLOAD,ArrecadacaoHttpTest.PAYLOAD,BaixaHttpTest.PAYLOAD,SaldoHttpTest.PAYLOAD,PrazoHttpTest.PAYLOAD)) lots.add(remessas.receive(token,key,new LotePayload().read(payload.getBytes(StandardCharsets.UTF_8))));
        return new Legacy(source,token,key,lots,remessas.inspect(ADMIN,lots.getFirst().number()).events());
    }
    @Test void migraV7ComSeisAssuntosSemRecriarDadosOuExecutarTrabalhos() throws Exception {
        var old=legacySeven();var jdbc=new JdbcTemplate(old.source);var money=jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo");var sessions=jdbc.queryForList("SELECT * FROM sessions");var checksums=jdbc.queryForList("SELECT * FROM schema_migrations ORDER BY version");
        jdbc.update("UPDATE sqlite_sequence SET seq=42 WHERE name='lotes'");jdbc.update("UPDATE sqlite_sequence SET seq=77 WHERE name='remessa_eventos'");
        var migrated=fixture("cenario-a");assertEquals(10,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(checksums,jdbc.queryForList("SELECT * FROM schema_migrations WHERE version<=7 ORDER BY version"));assertEquals(money,jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo"));assertEquals(sessions,jdbc.queryForList("SELECT * FROM sessions"));
        for(var lot:old.lots) {var snapshot=migrated.processing.inspect(ADMIN,lot.number());assertEquals(Processing.State.RECEBIDO,snapshot.state());assertArrayEquals(lot.bytes(),snapshot.received().lote().bytes());assertEquals(lot.hash(),snapshot.received().lote().hash());assertEquals(lot.subject(),snapshot.received().lote().subject());assertEquals(old.events,snapshot.received().events());assertTrue(snapshot.effective().isEmpty());}
        assertEquals(Processing.Mode.MANUAL,migrated.processing.configuration(ADMIN).modo());assertEquals(1000,migrated.processing.configuration(ADMIN).latenciaMilissegundos());
        var next=migrated.receive(old.token,old.key,PrazoHttpTest.PAYLOAD);assertEquals(43,next.number());assertEquals(78,migrated.processing.inspect(ADMIN,next.number()).received().events().getLast().id());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos",Integer.class));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void falhaIntermediariaV8ReverteTudoEChecksumAntigoContinuaObrigatorio() throws Exception {
        var old=legacySeven();var jdbc=new JdbcTemplate(old.source);var money=jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo");
        jdbc.execute("CREATE VIEW registros_efetivos AS SELECT 1 AS conflito");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));
        assertEquals(7,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(7,jdbc.queryForObject("SELECT COUNT(*) FROM schema_migrations",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE name IN ('processamento_config','relogio_negocio','processamento_trabalhos','registros_processados')",Integer.class));assertEquals(money,jdbc.queryForList("SELECT * FROM valores_recebidos ORDER BY lote_id,indice,campo"));
        jdbc.execute("DROP VIEW registros_efetivos");String checksum=jdbc.queryForObject("SELECT checksum FROM schema_migrations WHERE version=7",String.class);jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=7");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));assertEquals(7,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        jdbc.update("UPDATE schema_migrations SET checksum=? WHERE version=7",checksum);assertNotNull(fixture("cenario-a").processing.start(old.token,old.key));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
}
