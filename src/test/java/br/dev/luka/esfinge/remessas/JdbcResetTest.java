package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.*;
import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.autenticacao.domain.*;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.application.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class JdbcResetTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var h=new JdbcFinalizationTest();h.temporary=temporary;return h;}
    static ScenarioService inspection(Fixture f) {return new ScenarioService(new JdbcScenarioRepository(f.source()),"cenario-a",ADMIN,f.auth());}
    static void await(CountDownLatch latch) {try {assertTrue(latch.await(10,TimeUnit.SECONDS));}catch(InterruptedException failure){Thread.currentThread().interrupt();throw new IllegalStateException(failure);}}
    static Map<String,List<Map<String,Object>>> scoped(JdbcTemplate jdbc,String scenario) {
        var result=new TreeMap<String,List<Map<String,Object>>>();
        snapshot(jdbc).forEach((table,rows)->{if(!table.equals("sqlite_sequence") && !table.equals("schema_migrations")) result.put(table,rows.stream().filter(row->scenario.equals(row.get("cenario")) || scenario.equals(row.get("scenario_id")) || table.equals("scenarios") && scenario.equals(row.get("id"))).toList());});return result;
    }
    @Test void resetPurgarEstadosMisturadosPreservaConfigIdentidadesOutroCenarioEReinicio() {
        var h=helper();var f=h.fixture("cenario-a");String old=f.token("0001"),expired=f.remessas().open(old,202606,"NAO").key();f.receive(old,expired,BaixaHttpTest.PAYLOAD);
        new JdbcTransactions(f.source()).execute(()->{f.clock().advance(259200000);return null;});String token=f.token("0001");f.processing().consult(token,1);
        String success=f.remessas().open(token,202601,"NAO").key();for(var subject:Assunto.values())f.receive(token,success,JdbcSubjectDiscardTest.payload(subject));h.complete(f,token,success);
        f.processing().configure(ADMIN,Processing.Mode.MANUAL,12345);String active=f.remessas().open(token,202602,"NAO").key();f.receive(token,active,PrazoHttpTest.PAYLOAD);f.processing().start(token,active);f.processing().advance(ADMIN,0,1);
        String lost=f.remessas().open(token,202603,"NAO").key();f.remessas().planReception(ADMIN,lost,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,1);assertThrows(Reception.Interrupted.class,()->f.receive(token,lost,RemessaHttpTest.PAYLOAD));
        String failed=f.remessas().open(token,202604,"NAO").key();var bad=f.receive(token,failed,CreditoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,bad.number(),0);f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);f.processing().start(token,failed);f.processing().advance(ADMIN,0,2);
        assertEquals(Processing.State.LOTE_DESCARTADO,f.processing().inspect(ADMIN,bad.number()).state());f.remessas().blockCompetencia(ADMIN,"0001",202602,true);f.processing().configure(ADMIN,Processing.Mode.AUTOMATICO,12345);
        var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,SaldoHttpTest.PAYLOAD);h.complete(b,bt,bk);
        var jdbc=new JdbcTemplate(f.source());var other=scoped(jdbc,"cenario-b");var identities=jdbc.queryForList("SELECT * FROM users ORDER BY scenario_id,user_id");var ugs=jdbc.queryForList("SELECT * FROM user_ugs ORDER BY scenario_id,user_id,ug");var migrations=jdbc.queryForList("SELECT * FROM schema_migrations ORDER BY version");var sequences=jdbc.queryForList("SELECT * FROM sqlite_sequence ORDER BY name");var configured=inspection(f).inspect(ADMIN);
        var reset=f.processing().reset(ADMIN,"cenario-a",0);assertEquals(new Processing.Reset("cenario-a",0,1,5,10,2),reset);
        assertEquals(other,scoped(jdbc,"cenario-b"));assertEquals(identities,jdbc.queryForList("SELECT * FROM users ORDER BY scenario_id,user_id"));assertEquals(ugs,jdbc.queryForList("SELECT * FROM user_ugs ORDER BY scenario_id,user_id,ug"));assertEquals(migrations,jdbc.queryForList("SELECT * FROM schema_migrations ORDER BY version"));assertEquals(sequences,jdbc.queryForList("SELECT * FROM sqlite_sequence ORDER BY name"));
        for(String table:List.of("registros_efetivos","registros_processados","valores_recebidos","falhas_processamento","processamento_trabalhos","falhas_recepcao","remessa_eventos","lotes","remessas","competencias_bloqueadas"))assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE cenario='cenario-a'",Integer.class),table);
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM sessions WHERE scenario_id='cenario-a'",Integer.class));assertEquals(INITIAL,f.clock().instant());assertEquals(Processing.Mode.MANUAL,f.processing().configuration(ADMIN).modo());assertEquals(1000,f.processing().configuration(ADMIN).latenciaMilissegundos());
        var after=inspection(f).inspect(ADMIN);assertEquals(configured.profile(),after.profile());assertEquals(configured.seed(),after.seed());assertEquals(configured.initialTime(),after.initialTime());assertEquals(configured.identities(),after.identities());assertEquals(configured.tokenSeconds(),after.tokenSeconds());assertEquals(1,after.generation());
        var saved=snapshot(jdbc);var restart=h.fixture("cenario-a");assertEquals(saved,snapshot(jdbc));assertEquals(0,restart.processing().advance(ADMIN,0,100).passosExecutados());assertThrows(AuthenticationFailure.class,()->restart.auth().authenticate(token));assertEquals(Processing.State.PROCESSADO_SUCESSO,b.processing().inspect(ADMIN,bl.number()).state());assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @ParameterizedTest @EnumSource(Assunto.class)
    void repeticaoDepoisDeResetProduzMesmoConteudoETotaisExatos(Assunto subject) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();String payload=JdbcSubjectDiscardTest.payload(subject);var old=f.receive(token,key,payload);h.complete(f,token,key);var before=f.processing().inspect(ADMIN,old.number());
        f.processing().reset(ADMIN,"cenario-a",0);String fresh=f.token("0001"),newKey=f.remessas().open(fresh,202601,"NAO").key();var next=f.receive(fresh,newKey,payload);h.complete(f,fresh,newKey);var after=f.processing().inspect(ADMIN,next.number());
        assertTrue(next.number()>old.number());assertNotEquals(key,newKey);assertEquals(before.state(),after.state());assertArrayEquals(before.received().lote().bytes(),after.received().lote().bytes());assertEquals(before.received().lote().hash(),after.received().lote().hash());assertEquals(before.received().lote().records(),after.received().lote().records());assertEquals(before.effective().size(),after.effective().size());
        for(int i=0;i<before.effective().size();i++)assertArrayEquals(before.effective().get(i),after.effective().get(i));
        var totals=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var value:Assunto.values())totals.put(value,java.math.BigInteger.ZERO);totals.put(subject,java.math.BigInteger.ONE);f.remessas().finish(fresh,newKey,new Finalization(totals));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaDepoisDeTodosParticipantesReverteResetInteiro(boolean commit) {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.receive(token,key,CreditoHttpTest.PAYLOAD);f.processing().configure(ADMIN,Processing.Mode.AUTOMATICO,987);f.remessas().blockCompetencia(ADMIN,"0001",202601,true);new JdbcTransactions(f.source()).execute(()->{f.clock().advance(12345);return null;});var jdbc=new JdbcTemplate(f.source());
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id TEXT REFERENCES scenarios(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER DELETE ON sessions WHEN OLD.scenario_id='cenario-a' BEGIN "+(commit?"INSERT INTO falha_commit VALUES('cenario-inexistente');":"SELECT RAISE(ABORT,'falha ficticia');")+" END");var before=snapshot(jdbc);
        assertThrows(RuntimeException.class,()->f.processing().reset(ADMIN,"cenario-a",0));assertEquals(before,snapshot(jdbc));assertDoesNotThrow(()->f.auth().authenticate(token));jdbc.execute("DROP TRIGGER impedir");assertEquals(1,f.processing().reset(ADMIN,"cenario-a",0).geracao());
    }
    @Test void highWaterAcimaDoMaxETabelasVaziasNaoReutilizamIds() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.receive(token,key,RemessaHttpTest.PAYLOAD);h.complete(f,token,key);var jdbc=new JdbcTemplate(f.source());jdbc.update("UPDATE sqlite_sequence SET seq=900");var sequence=jdbc.queryForList("SELECT * FROM sqlite_sequence ORDER BY name");
        f.processing().reset(ADMIN,"cenario-a",0);assertEquals(sequence,jdbc.queryForList("SELECT * FROM sqlite_sequence ORDER BY name"));var restarted=h.fixture("cenario-a");String fresh=restarted.token("0001"),next=restarted.remessas().open(fresh,202601,"NAO").key();assertTrue(restarted.receive(fresh,next,RemessaHttpTest.PAYLOAD).number()>900);restarted.processing().start(fresh,next);
        for(String table:List.of("remessas","remessa_eventos","processamento_trabalhos"))assertTrue(jdbc.queryForObject("SELECT MIN(id) FROM "+table,Long.class)>900,table);
    }
    @Test void geracaoObsoletaAlvoIncorretoEOverflowNaoApagamExecucao() {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.receive(token,key,RemessaHttpTest.PAYLOAD);var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);
        for(String target:List.of("cenario-b","cenario-a"))assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().reset(ADMIN,target,1)).kind());assertEquals(before,snapshot(jdbc));
        jdbc.update("UPDATE scenarios SET generation=? WHERE id='cenario-a'",Long.MAX_VALUE);before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().reset(ADMIN,"cenario-a",Long.MAX_VALUE)).kind());assertEquals(before,snapshot(jdbc));
    }
    @Test void trabalhoCapturadoAntesDeResetNaoGravaDepoisDeNovaExecucaoNemReinicio() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing().start(token,key);var repo=new JdbcProcessingRepository(f.source());var received=repo.next("cenario-a",INITIAL).orElseThrow();f.processing().advance(ADMIN,0,1);var work=repo.next("cenario-a",INITIAL).orElseThrow();var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var scenario=inspection(f);
        var paused=new ProcessingService(f.auth(),scenario,f.remessas(),new JdbcRemessaRepository(f.source()),repo,new JdbcTransactions(f.source()),f.clock(),f.clock(),lot->{entered.countDown();await(release);return new StructuralRecords().process(lot);},"cenario-a",f.auth(),scenario);
        try(var pool=Executors.newSingleThreadExecutor()) {
            var pending=pool.submit(()->paused.advance(ADMIN,0,1));await(entered);
            try {h.fixture("cenario-a").processing().reset(ADMIN,"cenario-a",0);String fresh=f.token("0001"),newKey=f.remessas().open(fresh,202601,"NAO").key();f.receive(fresh,newKey,CreditoHttpTest.PAYLOAD);var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);release.countDown();assertEquals(0,pending.get(10,TimeUnit.SECONDS).passosExecutados());assertEquals(before,snapshot(jdbc));h.fixture("cenario-a");assertFalse(repo.begin(received,INITIAL,INITIAL));assertFalse(repo.complete(work,List.of("{}".getBytes(StandardCharsets.UTF_8)),INITIAL));assertFalse(repo.fail(work,INITIAL));assertEquals(before,snapshot(jdbc));}
            finally {release.countDown();}
        }
    }
    @Test void aberturaComPreflightAnteriorAoResetReautenticaAntesDeInserir() throws Exception {
        var f=helper().fixture("cenario-a");String token=f.token("0001");var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var first=new AtomicBoolean(true);
        Authentication delayed=new Authentication() {
            public Session authenticate(String value) {var session=f.auth().authenticate(value);if(first.compareAndSet(true,false)){entered.countDown();await(release);}return session;}
            public String login(String code,String password,String ug) {return f.auth().login(code,password,ug);}
        };
        var remessas=new RemessaService(delayed,new JdbcRemessaRepository(f.source()),new JdbcTransactions(f.source()),f.clock(),inspection(f),new JdbcProcessingRepository(f.source()));
        try(var pool=Executors.newSingleThreadExecutor()) {
            var opening=pool.submit(()->remessas.open(token,202601,"NAO"));await(entered);
            try {f.processing().reset(ADMIN,"cenario-a",0);release.countDown();var failure=assertThrows(ExecutionException.class,()->opening.get(10,TimeUnit.SECONDS));assertInstanceOf(AuthenticationFailure.class,failure.getCause());assertEquals(0,new JdbcTemplate(f.source()).queryForObject("SELECT COUNT(*) FROM remessas",Integer.class));}
            finally {release.countDown();}
        }
    }
    @Test void aberturaQuePossuiTransacaoAntesDoResetEConcluidaERemovida() throws Exception {
        var f=helper().fixture("cenario-a");String token=f.token("0001");var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        var repository=spy(new JdbcRemessaRepository(f.source()));doAnswer(call->{entered.countDown();await(release);return call.callRealMethod();}).when(repository).open(anyString(),anyString(),anyInt(),anyString(),any(Instant.class));
        var remessas=new RemessaService(f.auth(),repository,new JdbcTransactions(f.source()),f.clock(),inspection(f),new JdbcProcessingRepository(f.source()));
        try(var pool=Executors.newFixedThreadPool(2)) {
            var opening=pool.submit(()->remessas.open(token,202601,"NAO"));await(entered);var started=new CountDownLatch(1);var reset=pool.submit(()->{started.countDown();return f.processing().reset(ADMIN,"cenario-a",0);});await(started);
            try {release.countDown();assertNotNull(opening.get(10,TimeUnit.SECONDS));assertEquals(1,reset.get(10,TimeUnit.SECONDS).remessasRemovidas());assertEquals(0,new JdbcTemplate(f.source()).queryForObject("SELECT COUNT(*) FROM remessas",Integer.class));assertThrows(AuthenticationFailure.class,()->f.auth().authenticate(token));}
            finally {release.countDown();}
        }
    }
    @Test void loginComTempoCapturadoEmTransacaoTerminaAntesDeResetESeuTokenEInvalidado() throws Exception {
        var f=helper().fixture("cenario-a");new JdbcTransactions(f.source()).execute(()->{f.clock().advance(259200000);return null;});var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        Clock delayed=new Clock() {
            public ZoneId getZone(){return ZoneOffset.UTC;}
            public Clock withZone(ZoneId zone){return this;}
            public Instant instant(){assertTrue(TransactionSynchronizationManager.isActualTransactionActive());var now=f.clock().instant();entered.countDown();await(release);return now;}
        };
        var auth=new AuthenticationService(new JdbcAuthenticationRepository(f.source()),delayed,"cenario-a",Duration.ofHours(1),new JdbcAuthenticationTransactions(f.source()));
        try(var pool=Executors.newFixedThreadPool(2)) {
            var login=pool.submit(()->auth.login("acesso-ficticio","senha-ficticia","0001"));await(entered);var started=new CountDownLatch(1);var reset=pool.submit(()->{started.countDown();return f.processing().reset(ADMIN,"cenario-a",0);});await(started);
            try {release.countDown();String token=login.get(10,TimeUnit.SECONDS);assertEquals(1,reset.get(10,TimeUnit.SECONDS).sessoesInvalidadas());assertThrows(AuthenticationFailure.class,()->f.auth().authenticate(token));assertEquals(INITIAL,f.clock().instant());}
            finally {release.countDown();}
        }
    }
    @Test void loginDepoisDeResetUsaTempoNovoETtlSemOffsetAntigo() throws Exception {
        var f=helper().fixture("cenario-a");f.token("0001");new JdbcTransactions(f.source()).execute(()->{f.clock().advance(259200000);return null;});var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var scenario=inspection(f);
        var resetter=new ProcessingService(f.auth(),scenario,f.remessas(),new JdbcRemessaRepository(f.source()),new JdbcProcessingRepository(f.source()),new JdbcTransactions(f.source()),f.clock(),f.clock(),new StructuralRecords(),"cenario-a",()->{long count=f.auth().invalidateSessions();entered.countDown();await(release);return count;},scenario);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var reset=pool.submit(()->resetter.reset(ADMIN,"cenario-a",0));await(entered);var started=new CountDownLatch(1);var login=pool.submit(()->{started.countDown();return f.token("0001");});await(started);
            try {release.countDown();assertEquals(1,reset.get(10,TimeUnit.SECONDS).geracao());var session=f.auth().authenticate(login.get(10,TimeUnit.SECONDS));assertEquals(INITIAL,session.issuedAt());assertEquals(INITIAL.plusSeconds(3600),session.expiresAt());}
            finally {release.countDown();}
        }
    }
    @Test void resetsConcorrentesSoUmaGeracaoVence() throws Exception {
        var h=helper();var a=h.fixture("cenario-a");a.token("0001");var b=h.fixture("cenario-a");var result=race(()->a.processing().reset(ADMIN,"cenario-a",0),()->b.processing().reset(ADMIN,"cenario-a",0));assertEquals(1,result.stream().filter(Processing.Reset.class::isInstance).count());assertEquals(1,result.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());assertEquals(1,inspection(a).inspect(ADMIN).generation());
    }
    @Test void resetEmUmaBaseNaoAlteraOutraInstanciaComMesmoCenario() throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String at=a.token("0001"),ak=a.remessas().open(at,202601,"NAO").key();a.receive(at,ak,CreditoHttpTest.PAYLOAD);
        var separate=new JdbcFinalizationTest();separate.temporary=Files.createDirectory(temporary.resolve("outra-base"));var b=separate.fixture("cenario-a");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,SaldoHttpTest.PAYLOAD);separate.complete(b,bt,bk);var jdbc=new JdbcTemplate(b.source());var before=snapshot(jdbc);
        a.processing().reset(ADMIN,"cenario-a",0);assertEquals(before,snapshot(jdbc));assertDoesNotThrow(()->b.auth().authenticate(bt));assertEquals(Processing.State.PROCESSADO_SUCESSO,b.processing().inspect(ADMIN,bl.number()).state());
    }
}
