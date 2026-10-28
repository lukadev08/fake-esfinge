package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.configuration.SqliteDatabase;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.application.ProcessingService;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;

class JdbcExpirationTest {
    @TempDir Path temporary;
    static final long HOURS_72=259200000;
    JdbcFinalizationTest helper() {var helper=new JdbcFinalizationTest();helper.temporary=temporary;return helper;}
    void move(Fixture f,long millis) {new JdbcTransactions(f.source()).execute(()->{f.clock().advance(millis);return null;});}
    Remessa remit(Fixture f,String key) {return new JdbcRemessaRepository(f.source()).find(key,"cenario-a","0001").orElseThrow();}
    void assertDeadline(Fixture f,String key,Instant activity) {
        var remit=remit(f,key);assertEquals(activity,remit.lastActivity());assertEquals(Remessa.deadline(activity),remit.expiresAt());
    }
    @Test void mutacaoVencidaConfirmaExpiracaoAntesDe409PreservaHistoricoEIsolaOutrosContextos() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();
        f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var successful=f.receive(token,key,CreditoHttpTest.PAYLOAD);f.processing().start(token,key);f.processing().advance(ADMIN,0,2);
        var pending=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.remessas().planReception(ADMIN,key,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,Reception.Type.APOS_COMMIT,2);
        String other=f.token("0002"),otherKey=f.remessas().open(other,202601,"NAO").key();var otherLot=f.receive(other,otherKey,RemessaHttpTest.PAYLOAD);
        var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,RemessaHttpTest.PAYLOAD);
        move(f,HOURS_72);String fresh=f.token("0001");
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.receive(fresh,key,SaldoHttpTest.PAYLOAD)).kind());
        var closed=remit(f,key);assertEquals(INITIAL.plusSeconds(259200),closed.expiredAt());assertNull(closed.finalizedAt());assertNull(closed.cancelledAt());assertEquals(INITIAL,closed.lastActivity());
        var oldSuccess=f.processing().inspect(ADMIN,successful.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,oldSuccess.state());assertFalse(oldSuccess.active());assertNull(oldSuccess.diagnostic());
        var oldPending=f.processing().inspect(ADMIN,pending.number());assertEquals(Processing.State.ABORTADO,oldPending.state());assertEquals("SIM_ENVIO_EXPIRADO",oldPending.diagnostic().codigo());assertFalse(oldPending.active());
        assertTrue(oldSuccess.processed().isEmpty());assertTrue(oldSuccess.effective().isEmpty());assertTrue(oldPending.processed().isEmpty());assertArrayEquals(SaldoHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8),oldPending.received().lote().bytes());
        assertTrue(f.processing().inspect(ADMIN,otherLot.number()).active());assertTrue(b.processing().inspect(ADMIN,bl.number()).active());
        var jdbc=new JdbcTemplate(f.source());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_EXPIRADO'",Integer.class));
        var before=snapshot(jdbc);assertEquals(Processing.State.ABORTADO,f.processing().consult(fresh,pending.number()).state());assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().cancel(fresh,key)).kind());assertEquals(before,snapshot(jdbc));
        assertNotEquals(key,f.remessas().open(fresh,202601,"NAO").key());assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @ParameterizedTest @ValueSource(strings={"RECEIVE","START","DISCARD","CONSULT","FINISH","CANCEL"})
    void cadaOperacaoCompativelBemSucedidaRenovaAtividadeEAgendaAtomicamente(String operation) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);
        if(operation.equals("FINISH")) h.complete(f,token,key);
        move(f,3599999);var now=f.clock().instant();
        switch(operation) {
            case "RECEIVE" -> f.receive(token,key,RemessaHttpTest.PAYLOAD);
            case "START" -> f.processing().start(token,key);
            case "DISCARD" -> f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);
            case "CONSULT" -> f.processing().consult(token,lot.number());
            case "FINISH" -> f.remessas().finish(token,key,totals(1));
            case "CANCEL" -> f.processing().cancel(token,key);
            default -> throw new AssertionError(operation);
        }
        assertDeadline(f,key,now);
    }
    @Test void autenticacaoIsolamentoBloqueioEAdministracaoNaoExpiramNemRenovamChave() {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);
        f.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,2);f.processing().planFailure(ADMIN,lot.number(),null);
        move(f,HOURS_72);String fresh=f.token("0001"),other=f.token("0002");f.remessas().blockCompetencia(ADMIN,"0001",202601,true);
        var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);
        assertThrows(AuthenticationFailure.class,()->f.receive(token,key,RemessaHttpTest.PAYLOAD));assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->f.receive(other,key,RemessaHttpTest.PAYLOAD)).kind());assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.BLOCKED,assertThrows(RemessaFailure.class,()->f.remessas().open(fresh,202601,"NAO")).kind());assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,1)).kind());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.remessas().removeReceptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE)).kind());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().planFailure(ADMIN,lot.number(),0)).kind());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().removeFailure(ADMIN,lot.number())).kind());
        assertNotNull(f.remessas().receptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE));assertNotNull(f.processing().failurePlan(ADMIN,lot.number()));
        assertEquals(Processing.State.RECEBIDO,f.processing().inspect(ADMIN,lot.number()).state());assertEquals(before,snapshot(jdbc));assertNull(remit(f,key).expiredAt());
        assertEquals(Processing.State.ABORTADO,f.processing().consult(fresh,lot.number()).state());assertNotNull(remit(f,key).expiredAt());
    }
    @ParameterizedTest @ValueSource(strings={"RECEIVE","START","DISCARD","FINISH","CANCEL"})
    void mutacoesComChaveVencidaExpiramAntesDoBloqueioEConfirmamEncerramento(String operation) {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.receive(token,key,RemessaHttpTest.PAYLOAD);
        f.remessas().blockCompetencia(ADMIN,"0001",202601,true);move(f,HOURS_72);String fresh=f.token("0001");
        var failure=assertThrows(RemessaFailure.class,()->{switch(operation) {
            case "RECEIVE" -> f.receive(fresh,key,RemessaHttpTest.PAYLOAD);
            case "START" -> f.processing().start(fresh,key);
            case "DISCARD" -> f.processing().discard(fresh,key,Assunto.CADASTRO_CONTRIBUINTE);
            case "FINISH" -> f.remessas().finish(fresh,key,totals(1));
            case "CANCEL" -> f.processing().cancel(fresh,key);
            default -> throw new AssertionError(operation);
        }});
        assertEquals(RemessaFailure.Kind.CONFLICT,failure.kind());assertNotNull(remit(f,key).expiredAt());assertEquals(INITIAL,remit(f,key).lastActivity());
        var before=snapshot(new JdbcTemplate(f.source()));assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.receive(fresh,key,RemessaHttpTest.PAYLOAD)).kind());assertEquals(before,snapshot(new JdbcTemplate(f.source())));
    }
    @Test void consultaAntesDoPrazoRenovaEAgendaRelogioSobrevivemAoReinicio() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);
        move(f,HOURS_72-1);String fresh=f.token("0001");assertEquals(Processing.State.RECEBIDO,f.processing().consult(fresh,lot.number()).state());var renewed=f.clock().instant();assertDeadline(f,key,renewed);
        var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);var restarted=h.fixture("cenario-a");assertEquals(before,snapshot(jdbc));assertEquals(renewed,restarted.clock().instant());assertDeadline(restarted,key,renewed);
        restarted.processing().advance(ADMIN,HOURS_72-1,1);assertNull(remit(restarted,key).expiredAt());restarted.processing().advance(ADMIN,1,1);
        assertNotNull(remit(restarted,key).expiredAt());assertEquals(Processing.State.ABORTADO,restarted.processing().inspect(ADMIN,lot.number()).state());
    }
    @ParameterizedTest @EnumSource(Processing.Mode.class)
    void tickVerificaAgendaNosDoisModosSemAvancarRelogioEFinalizadasNaoExpiram(Processing.Mode mode) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);
        f.processing().configure(ADMIN,mode,HOURS_72+1);f.processing().start(token,key);move(f,HOURS_72-1);var before=f.clock().instant();f.processing().tick();assertEquals(before,f.clock().instant());assertNull(remit(f,key).expiredAt());
        assertEquals(mode==Processing.Mode.MANUAL?Processing.State.RECEBIDO:Processing.State.EM_PROCESSAMENTO,f.processing().inspect(ADMIN,lot.number()).state());
        move(f,1);f.processing().tick();assertEquals(Processing.State.ABORTADO,f.processing().inspect(ADMIN,lot.number()).state());assertTrue(f.processing().inspect(ADMIN,lot.number()).effective().isEmpty());
        String fresh=f.token("0001"),done=f.remessas().open(fresh,202601,"NAO").key();f.receive(fresh,done,RemessaHttpTest.PAYLOAD);h.complete(f,fresh,done);f.remessas().finish(fresh,done,totals(1));
        String canceled=f.remessas().open(fresh,202602,"NAO").key();f.processing().cancel(fresh,canceled);move(f,HOURS_72+1);f.processing().tick();assertNull(remit(f,done).expiredAt());assertNull(remit(f,canceled).expiredAt());
    }
    @Test void varreduraUsaSomenteCenarioAtualEDeadlineProprioDeCadaUg() {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();var lot=a.receive(token,key,RemessaHttpTest.PAYLOAD);
        String other=a.token("0002"),otherKey=a.remessas().open(other,202601,"NAO").key();var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,RemessaHttpTest.PAYLOAD);
        move(a,3600000);var otherLot=a.receive(a.token("0002"),otherKey,RemessaHttpTest.PAYLOAD);move(a,HOURS_72-3600000);a.processing().tick();
        assertEquals(Processing.State.ABORTADO,a.processing().inspect(ADMIN,lot.number()).state());assertTrue(a.processing().inspect(ADMIN,otherLot.number()).active());assertEquals(INITIAL,b.clock().instant());assertTrue(b.processing().inspect(ADMIN,bl.number()).active());
        move(a,3600000);a.processing().tick();assertFalse(a.processing().inspect(ADMIN,otherLot.number()).active());assertTrue(b.processing().inspect(ADMIN,bl.number()).active());
    }
    @ParameterizedTest @ValueSource(strings={"BEGIN","COMPLETE","FAIL"})
    void gravaçõesTardiasChecamPrazoMesmoAntesDeMaterializarExpiracao(String operation) {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);
        f.processing().planFailure(ADMIN,lot.number(),null);f.processing().start(token,key);var repo=new JdbcProcessingRepository(f.source());var tx=new JdbcTransactions(f.source());var received=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());
        if(!operation.equals("BEGIN")) assertTrue(tx.execute(()->repo.begin(received,INITIAL,INITIAL)));
        var work=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());move(f,HOURS_72);var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);var now=f.clock().instant();
        assertFalse(tx.execute(()->switch(operation) {case "BEGIN"->repo.begin(work,INITIAL,now);case "COMPLETE"->repo.complete(work,new StructuralRecords().process(lot),now);case "FAIL"->repo.fail(work,now);default->throw new AssertionError(operation);}));
        assertEquals(before,snapshot(jdbc));assertNull(remit(f,key).expiredAt());f.processing().tick();assertNotNull(remit(f,key).expiredAt());
    }
    @Test void calculoPausadoForaDaTransacaoNaoReaplicaEfeitosDepoisDeExpirar() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,CreditoHttpTest.PAYLOAD);f.processing().start(token,key);f.processing().advance(ADMIN,0,1);
        var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var inspection=new ScenarioService(new JdbcScenarioRepository(f.source()),"cenario-a",ADMIN,f.auth());
        var paused=new ProcessingService(f.auth(),inspection,f.remessas(),new JdbcRemessaRepository(f.source()),new JdbcProcessingRepository(f.source()),new JdbcTransactions(f.source()),f.clock(),f.clock(),received->{entered.countDown();try{if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException();}catch(InterruptedException failure){Thread.currentThread().interrupt();throw new IllegalStateException(failure);}return new StructuralRecords().process(received);},"cenario-a",f.auth(),inspection);
        try(var pool=Executors.newSingleThreadExecutor()) {
            var future=pool.submit(()->paused.advance(ADMIN,0,1));try {assertTrue(entered.await(10,TimeUnit.SECONDS));var other=h.fixture("cenario-a");other.processing().advance(ADMIN,HOURS_72,1);} finally {release.countDown();}
            assertEquals(0,future.get(10,TimeUnit.SECONDS).passosExecutados());
        }
        var state=f.processing().inspect(ADMIN,lot.number());assertEquals(Processing.State.ABORTADO,state.state());assertTrue(state.processed().isEmpty());assertTrue(state.effective().isEmpty());assertFalse(state.active());
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaDeStatementOuCommitReverteExpiracaoInteiraSem409Falso(boolean commit) {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.receive(token,key,SaldoHttpTest.PAYLOAD);move(f,HOURS_72);String fresh=f.token("0001");var jdbc=new JdbcTemplate(f.source());
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ENVIO_EXPIRADO' BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");var before=snapshot(jdbc);
        var failure=assertThrows(RuntimeException.class,()->f.receive(fresh,key,SaldoHttpTest.PAYLOAD));assertFalse(failure instanceof RemessaFailure);assertEquals(before,snapshot(jdbc));assertNull(remit(f,key).expiredAt());
        jdbc.execute("DROP TRIGGER impedir");assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.receive(fresh,key,SaldoHttpTest.PAYLOAD)).kind());assertNotNull(remit(f,key).expiredAt());
    }
    @Test void fronteiraDeNanosEEnvioVazioUsamAgendaExataSemPontoFlutuante() {
        var f=helper().fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var jdbc=new JdbcTemplate(f.source());var due=INITIAL.plusSeconds(259200).plusNanos(999999999);
        jdbc.update("UPDATE remessas SET atividade=?,expira_segundos=?,expira_nanos=? WHERE chave=?",INITIAL.plusNanos(999999999).toString(),due.getEpochSecond(),due.getNano(),key);var remit=remit(f,key);var tx=new JdbcTransactions(f.source());var repo=new JdbcProcessingRepository(f.source());var before=snapshot(jdbc);
        assertFalse(tx.execute(()->repo.expire(remit,0,due.minusNanos(1))));assertEquals(before,snapshot(jdbc));assertFalse(tx.execute(()->repo.expire(remit,1,due)));assertEquals(before,snapshot(jdbc));assertTrue(tx.execute(()->repo.expire(remit,0,due)));assertEquals(due,remit(f,key).expiredAt());
        before=snapshot(jdbc);assertFalse(tx.execute(()->repo.expire(remit,0,due.plusNanos(1))));assertEquals(before,snapshot(jdbc));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertNotEquals(key,f.remessas().open(token,202601,"NAO").key());
    }
    @Test void migraV13PreenchidaComNanosEAlemDoHorizonteEReverteBackfillInvalido() throws Exception {
        var old=new JdbcCancellationTest();old.temporary=temporary;var source=old.legacyTen();var jdbc=new JdbcTemplate(source);
        for(String script:List.of("V11__cancelamento.sql","V12__falhas_recepcao.sql","V13__bloqueio_competencia.sql")) {
            var resource=new ClassPathResource("db/migration/"+script);try(var c=source.getConnection()){ScriptUtils.executeSqlScript(c,resource);}jdbc.update("INSERT INTO schema_migrations VALUES(?,?)",Integer.parseInt(script.substring(1,3)),CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        }
        long id=jdbc.queryForObject("SELECT MIN(id) FROM remessas",Long.class);jdbc.update("UPDATE remessas SET atividade='invalida' WHERE id=?",id);var before=snapshot(jdbc);var schema=jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(helper().settings("cenario-a")));assertEquals(13,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(before,snapshot(jdbc));assertEquals(schema,jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name"));
        var last=Instant.parse("9999-12-31T23:59:59.999999999Z");jdbc.update("UPDATE remessas SET atividade=? WHERE id=?",last.toString(),id);before=snapshot(jdbc);helper().fixture("cenario-a");assertEquals(14,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        var after=snapshot(jdbc);removeExpirationMigration(after);assertEquals(before,after);assertEquals(Remessa.deadline(last).getEpochSecond(),jdbc.queryForObject("SELECT expira_segundos FROM remessas WHERE id=?",Long.class,id));assertEquals(999999999,jdbc.queryForObject("SELECT expira_nanos FROM remessas WHERE id=?",Integer.class,id));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
}
