package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.configuration.*;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.application.ProcessingService;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.file.Path;
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

class JdbcCancellationTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var h=new JdbcFinalizationTest();h.temporary=temporary;return h;}
    static Finalization subjectTotals(Assunto subject,long count) {var values=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var name:Assunto.values())values.put(name,java.math.BigInteger.ZERO);values.put(subject,java.math.BigInteger.valueOf(count));return new Finalization(values);}
    @ParameterizedTest @EnumSource(Assunto.class)
    void seisAssuntosCancelamSucessosEmAgendadosERecebidosPreservandoOutrosContextos(Assunto subject) throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();
        var json=new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);var root=json.readTree(JdbcSubjectDiscardTest.payload(subject));var rows=(com.fasterxml.jackson.databind.node.ArrayNode)root.elements().next();rows.add(rows.get(0).deepCopy());String payload=root.toString();
        var done=a.receive(token,key,payload);h.complete(a,token,key);var pending=a.receive(token,key,payload);a.processing().planFailure(ADMIN,pending.number(),1);
        var queued=a.receive(token,key,JdbcSubjectDiscardTest.payload(Assunto.values()[(subject.ordinal()+1)%6]));
        String ug=a.token("0002"),uk=a.remessas().open(ug,202601,"NAO").key(),next=a.remessas().open(token,202602,"NAO").key();var ul=a.receive(ug,uk,payload);var nl=a.receive(token,next,payload);h.complete(a,token,next);a.remessas().finish(token,next,subjectTotals(subject,2));
        var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,payload);a.processing().configure(ADMIN,Processing.Mode.MANUAL,100);a.processing().start(token,key);a.processing().advance(ADMIN,0,1);var outside=a.receive(token,key,payload);
        var untouched=a.processing().inspect(ADMIN,ul.number());var closed=a.processing().inspect(ADMIN,nl.number());var other=b.processing().inspect(ADMIN,bl.number());var jdbc=new JdbcTemplate(a.source());var jobs=jdbc.queryForList("SELECT id,lote_id,devido_segundos,devido_nanos,latencia_ms,geracao FROM processamento_trabalhos ORDER BY id");
        var before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.processing().cancel(ug,key)).kind());assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.processing().cancel(bt,key)).kind());assertEquals(before,snapshot(jdbc));
        a.processing().cancel(token,key);var savedDone=a.processing().inspect(ADMIN,done.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,savedDone.state());assertNull(savedDone.diagnostic());
        for(var lot:List.of(done,pending,queued,outside)) {var saved=a.processing().inspect(ADMIN,lot.number());assertFalse(saved.active());assertTrue(saved.processed().isEmpty());assertTrue(saved.effective().isEmpty());assertArrayEquals(lot.bytes(),saved.received().lote().bytes());assertEquals(lot.hash(),saved.received().lote().hash());assertEquals(lot.records(),saved.received().lote().records());if(lot!=done){assertEquals(Processing.State.ABORTADO,saved.state());assertEquals("SIM_ENVIO_CANCELADO",saved.diagnostic().codigo());assertNull(saved.diagnostic().registro());}}
        assertEquals(1,a.processing().failurePlan(ADMIN,pending.number()).registro());assertEquals(jobs,jdbc.queryForList("SELECT id,lote_id,devido_segundos,devido_nanos,latencia_ms,geracao FROM processamento_trabalhos WHERE lote_id!=? ORDER BY id",outside.number()));
        JdbcSubjectDiscardTest.assertStable(untouched,a.processing().inspect(ADMIN,ul.number()));JdbcSubjectDiscardTest.assertStable(closed,a.processing().inspect(ADMIN,nl.number()));JdbcSubjectDiscardTest.assertStable(other,b.processing().inspect(ADMIN,bl.number()));
        for(String table:List.of("valores_recebidos","registros_processados","registros_efetivos"))assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE remessa_id=?",Integer.class,done.remessaId()));
        assertEquals(0,jdbc.queryForObject("SELECT generation FROM scenarios WHERE id='cenario-a'",Integer.class));var remessa=new JdbcRemessaRepository(a.source()).find(key,"cenario-a","0001").orElseThrow();assertNotNull(remessa.cancelledAt());assertNull(remessa.finalizedAt());
        var snapshot=snapshot(jdbc);var restarted=h.fixture("cenario-a");restarted.processing().consult(token,pending.number());assertEquals(snapshot,snapshot(jdbc));String replacement=restarted.remessas().open(token,202601,"NAO").key();assertNotEquals(key,replacement);var fresh=restarted.receive(token,replacement,payload);h.complete(restarted,token,replacement);restarted.remessas().finish(token,replacement,subjectTotals(subject,2));assertEquals(2,restarted.processing().inspect(ADMIN,fresh.number()).effective().size());assertTrue(fresh.number()>outside.number());assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void erroEDescarteAnterioresNaoSaoReescritosECancelamentoNaoAlcancaConcluido() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var bad=f.receive(token,key,SaldoHttpTest.PAYLOAD);var following=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,bad.number(),0);f.processing().start(token,key);f.processing().advance(ADMIN,0,100);
        var error=f.processing().inspect(ADMIN,bad.number());var aborted=f.processing().inspect(ADMIN,following.number());f.processing().cancel(token,key);JdbcSubjectDiscardTest.assertStable(error,f.processing().inspect(ADMIN,bad.number()));JdbcSubjectDiscardTest.assertStable(aborted,f.processing().inspect(ADMIN,following.number()));
        String closed=f.remessas().open(token,202601,"NAO").key();f.receive(token,closed,RemessaHttpTest.PAYLOAD);h.complete(f,token,closed);f.remessas().finish(token,closed,totals(1));var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().cancel(token,closed)).kind());assertEquals(before,snapshot(jdbc));
    }
    @ParameterizedTest @ValueSource(strings={"receive","start","complete","finish","open","cancel"})
    void concorrenciaDuasInstanciasLinearizaCancelamento(String action) throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=a.receive(token,key,RemessaHttpTest.PAYLOAD);
        if(action.equals("finish"))h.complete(a,token,key);if(action.equals("complete")){a.processing().start(token,key);a.processing().advance(ADMIN,0,1);}var b=h.fixture("cenario-a");
        var result=race(()->{a.processing().cancel(token,key);return "cancelled";},()->{switch(action){case "receive"->b.receive(token,key,RemessaHttpTest.PAYLOAD);case "start"->b.processing().start(token,key);case "complete"->b.processing().advance(ADMIN,0,10);case "finish"->b.remessas().finish(token,key,totals(1));case "open"->{return b.remessas().open(token,202601,"NAO").key();}default->{b.processing().cancel(token,key);return "cancelled";}}return action;});
        var jdbc=new JdbcTemplate(a.source());if(action.equals("finish")){assertEquals(1,result.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());if(result.contains("finish")){assertTrue(a.processing().inspect(ADMIN,lot.number()).active());return;}}
        assertTrue(result.contains("cancelled"));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_CANCELADO'",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM lotes WHERE remessa_id=? AND ativo=1",Integer.class,lot.remessaId()));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos WHERE remessa_id=?",Integer.class,lot.remessaId()));
        var before=snapshot(jdbc);assertEquals(0,h.fixture("cenario-a").processing().advance(ADMIN,0,100).passosExecutados());assertEquals(before,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void calculoPausadoForaTransacaoNaoGravaDepoisDoCancelamento() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().start(token,key);f.processing().advance(ADMIN,0,1);var other=h.fixture("cenario-a");var entered=new CountDownLatch(1);var release=new CountDownLatch(1);
        var inspection=new ScenarioService(new JdbcScenarioRepository(f.source()),"cenario-a",ADMIN,f.auth());var paused=new ProcessingService(f.auth(),inspection,f.remessas(),new JdbcRemessaRepository(f.source()),new JdbcProcessingRepository(f.source()),new JdbcTransactions(f.source()),f.clock(),f.clock(),received->{entered.countDown();try{if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException();}catch(InterruptedException failure){Thread.currentThread().interrupt();throw new IllegalStateException();}return new StructuralRecords().process(received);},"cenario-a");
        try(var pool=Executors.newSingleThreadExecutor()){var pending=pool.submit(()->paused.advance(ADMIN,0,1));try{assertTrue(entered.await(10,TimeUnit.SECONDS));other.processing().cancel(token,key);var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);release.countDown();assertEquals(0,pending.get(10,TimeUnit.SECONDS).passosExecutados());assertEquals(before,snapshot(jdbc));assertEquals(Processing.State.ABORTADO,other.processing().inspect(ADMIN,lot.number()).state());}finally{release.countDown();}}
    }
    @Test void trabalhosCapturadosNaoIniciamConcluemOuFalhamAposCancelamentoEReinicio() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,lot.number(),0);f.processing().start(token,key);var repo=new JdbcProcessingRepository(f.source());var tx=new JdbcTransactions(f.source());var received=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());tx.execute(()->repo.begin(received,INITIAL));var running=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());f.processing().cancel(token,key);h.fixture("cenario-a");var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);
        assertFalse(tx.execute(()->repo.begin(received,INITIAL)));assertFalse(tx.execute(()->repo.complete(running,new StructuralRecords().process(lot),INITIAL)));assertFalse(tx.execute(()->repo.fail(running,INITIAL)));assertEquals(before,snapshot(jdbc));
    }
    javax.sql.DataSource legacyTen() throws Exception {
        var previous=new JdbcProcessingFailureTest();previous.temporary=temporary;var source=previous.legacyNine();var resource=new ClassPathResource("db/migration/V10__erro_processamento.sql");try(var c=source.getConnection()){ScriptUtils.executeSqlScript(c,resource);}var jdbc=new JdbcTemplate(source);jdbc.update("INSERT INTO schema_migrations VALUES(10,?)",CredentialHash.token(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)));
        var ids=jdbc.queryForList("SELECT lote_id FROM processamento_trabalhos ORDER BY id",Long.class);for(int i=0;i<2;i++){long lot=ids.get(i);jdbc.update("UPDATE processamento_trabalhos SET situacao=?,codigo=?,mensagem=?,registro=? WHERE lote_id=?",i==0?"LOTE_DESCARTADO":"ABORTADO_ERRO_LOTE_ANTERIOR",i==0?"SIM_ERRO_INJETADO":"SIM_ABORTADO_ERRO_LOTE_ANTERIOR","Diagnostico ficticio preservado",i==0?0:null,lot);jdbc.update("DELETE FROM registros_efetivos WHERE lote_id=?",lot);jdbc.update("DELETE FROM registros_processados WHERE lote_id=?",lot);jdbc.update("DELETE FROM valores_recebidos WHERE lote_id=?",lot);jdbc.update("UPDATE lotes SET ativo=0 WHERE numero=?",lot);}
        long lot=ids.get(0);jdbc.update("INSERT INTO falhas_processamento(lote_id,remessa_id,cenario,ug,geracao,registro) SELECT numero,remessa_id,cenario,ug,0,0 FROM lotes WHERE numero=?",lot);jdbc.update("UPDATE remessas SET finalizada_em=?",INITIAL.toString());return source;
    }
    @Test void upgradeV10PreservaTodasTabelasDiagnosticosPlanosBytesEHighWater() throws Exception {
        var h=helper();var source=legacyTen();var jdbc=new JdbcTemplate(source);var before=snapshot(jdbc);var f=h.fixture("cenario-a");assertEquals(11,jdbc.queryForObject("PRAGMA user_version",Integer.class));var after=snapshot(jdbc);after.get("schema_migrations").removeLast();for(var row:after.get("remessas"))assertNull(row.remove("cancelada_em"));assertEquals(before,after);assertEquals(16,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE type='table'",Integer.class));
        assertEquals(Processing.Mode.AUTOMATICO,f.processing().configuration(ADMIN).modo());assertEquals(17,f.processing().configuration(ADMIN).latenciaMilissegundos());assertEquals(INITIAL.plusMillis(123),f.clock().instant());assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);f.processing().cancel(token,key);assertTrue(jdbc.queryForObject("SELECT id FROM processamento_trabalhos WHERE lote_id=?",Long.class,lot.number())>77);assertEquals(Processing.State.ABORTADO,f.processing().inspect(ADMIN,lot.number()).state());
        var saved=snapshot(jdbc);h.fixture("cenario-a");assertEquals(saved,snapshot(jdbc));jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=11");var corrupt=snapshot(jdbc);assertThrows(IllegalStateException.class,()->SqliteDatabase.open(h.settings("cenario-a")));assertEquals(corrupt,snapshot(jdbc));
    }
    @Test void highWaterPermaneceMesmoSemTrabalhosRestantes() throws Exception {
        var h=helper();var source=legacyTen();var jdbc=new JdbcTemplate(source);jdbc.execute("DELETE FROM processamento_trabalhos");assertEquals(77,jdbc.queryForObject("SELECT seq FROM sqlite_sequence WHERE name='processamento_trabalhos'",Integer.class));h.fixture("cenario-a");assertEquals(77,jdbc.queryForList("SELECT seq FROM sqlite_sequence WHERE name='processamento_trabalhos'",Integer.class).stream().findFirst().orElse(null));
    }
    @Test void falhaNoMeioDaV11ReverteMarcadorIndicesJobsSequenciasEVersao() throws Exception {
        var h=helper();var source=legacyTen();var jdbc=new JdbcTemplate(source);jdbc.execute("DROP INDEX trabalho_devido");var before=snapshot(jdbc);var schema=jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(h.settings("cenario-a")));assertEquals(10,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(before,snapshot(jdbc));assertEquals(schema,jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name"));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.execute("CREATE INDEX trabalho_devido ON processamento_trabalhos(cenario,devido_segundos,devido_nanos,id)");h.fixture("cenario-a");assertEquals(11,jdbc.queryForObject("PRAGMA user_version",Integer.class));
    }
    @Test void dezMilEUmRegistrosCanceladosConservamOriginalEReenvioNaoDeduplica() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var json=new com.fasterxml.jackson.databind.ObjectMapper();String row=json.readTree(RemessaHttpTest.PAYLOAD).get("cadastrosContribuintes").get(0).toString();String payload="{\"cadastrosContribuintes\":["+String.join(",",Collections.nCopies(10001,row))+"]}";
        var old=f.receive(token,key,payload);h.complete(f,token,key);f.processing().cancel(token,key);var historical=f.processing().inspect(ADMIN,old.number());assertFalse(historical.active());assertEquals(Processing.State.PROCESSADO_SUCESSO,historical.state());assertEquals(10001,historical.received().lote().records());assertTrue(historical.effective().isEmpty());assertArrayEquals(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8),historical.received().lote().bytes());
        String next=f.remessas().open(token,202601,"NAO").key();var fresh=f.receive(token,next,payload);h.complete(f,token,next);f.remessas().finish(token,next,totals(10001));assertNotEquals(key,next);assertEquals(old.hash(),fresh.hash());assertEquals(10001,f.processing().inspect(ADMIN,fresh.number()).effective().size());
    }
}
