package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.*;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;

class JdbcProcessingFailureTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var h=new JdbcFinalizationTest();h.temporary=temporary;return h;}
    @ParameterizedTest @ValueSource(ints={0,1,2})
    void primeiroIntermediarioUltimoPreservamAnterioresHistoricoEFinalizamAtivos(int position) throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);
        var lots=new ArrayList<Lote>();for(int i=0;i<3;i++) lots.add(f.receive(token,key,RemessaHttpTest.PAYLOAD));
        var other=f.receive(token,key,PrazoHttpTest.PAYLOAD);var target=lots.get(position);f.processing().planFailure(ADMIN,target.number(),null);
        var restarted=h.fixture("cenario-a");assertNull(restarted.processing().failurePlan(ADMIN,target.number()).registro());restarted.processing().start(token,key);restarted.processing().advance(ADMIN,0,100);
        for(int i=0;i<3;i++) {var snapshot=restarted.processing().inspect(ADMIN,lots.get(i).number());assertEquals(i<position,snapshot.active());assertEquals(i<position?1:0,snapshot.effective().size());assertArrayEquals(lots.get(i).bytes(),snapshot.received().lote().bytes());assertEquals(lots.get(i).hash(),snapshot.received().lote().hash());if(i>=position)assertNull(snapshot.diagnostic().registro());}
        assertEquals(1,restarted.processing().inspect(ADMIN,other.number()).effective().size());
        var values=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var subject:Assunto.values())values.put(subject,java.math.BigInteger.ZERO);values.put(Assunto.CADASTRO_CONTRIBUINTE,java.math.BigInteger.valueOf(position));values.put(Assunto.INICIO_PRAZO_PRESCRICIONAL,java.math.BigInteger.ONE);restarted.remessas().finish(token,key,new Finalization(values));
        var jdbc=new JdbcTemplate(f.source());var saved=snapshot(jdbc);assertEquals(0,h.fixture("cenario-a").processing().advance(ADMIN,0,100).passosExecutados());assertEquals(saved,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @ParameterizedTest @ValueSource(strings={"cadastro","credito","arrecadacao","baixa","saldo","prazo"})
    void seisAssuntosIndiceNaoZeroIsolamUgCenarioEEnvio(String subject) throws Exception {
        String payload=switch(subject){case "credito"->CreditoHttpTest.PAYLOAD;case "arrecadacao"->ArrecadacaoHttpTest.PAYLOAD;case "baixa"->BaixaHttpTest.PAYLOAD;case "saldo"->SaldoHttpTest.PAYLOAD;case "prazo"->PrazoHttpTest.PAYLOAD;default->RemessaHttpTest.PAYLOAD;};
        var json=new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);var root=json.readTree(payload);((com.fasterxml.jackson.databind.node.ArrayNode)root.elements().next()).add(root.elements().next().get(0).deepCopy());payload=root.toString();
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var target=f.receive(token,key,payload);var following=f.receive(token,key,payload);var original=target.bytes();
        String ug=f.token("0002"),ugKey=f.remessas().open(ug,202601,"NAO").key();var ugLot=f.receive(ug,ugKey,payload);String next=f.remessas().open(token,202602,"NAO").key();var nextLot=f.receive(token,next,payload);
        var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,payload);assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.processing().planFailure(ADMIN,target.number(),1)).kind());
        f.processing().planFailure(ADMIN,target.number(),1);f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);f.processing().start(token,key);f.processing().advance(ADMIN,0,100);
        assertEquals(1,f.processing().inspect(ADMIN,target.number()).diagnostic().registro());assertArrayEquals(original,f.processing().inspect(ADMIN,target.number()).received().lote().bytes());assertFalse(f.processing().inspect(ADMIN,following.number()).active());
        for(var lot:List.of(ugLot,nextLot))assertTrue(f.processing().inspect(ADMIN,lot.number()).active());assertTrue(b.processing().inspect(ADMIN,bl.number()).active());
        var jdbc=new JdbcTemplate(f.source());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos WHERE lote_id IN (?,?)",Integer.class,target.number(),following.number()));
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().start(token,key)).kind());assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.remessas().finish(token,key,totals(0))).kind());
    }
    @Test void posterioresEmAndamentoEForaSnapshotAbortamMasSucessoPosteriorPermanece() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);
        var target=f.receive(token,key,SaldoHttpTest.PAYLOAD);var running=f.receive(token,key,SaldoHttpTest.PAYLOAD);var done=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,target.number(),0);f.processing().start(token,key);
        var repo=new JdbcProcessingRepository(f.source());var tx=new JdbcTransactions(f.source());
        // Exercita a mesma porta com trabalhos posteriores em fases concorrentes.
        var jdbc=new JdbcTemplate(f.source());jdbc.update("UPDATE processamento_trabalhos SET situacao='EM_PROCESSAMENTO' WHERE lote_id IN (?,?)",running.number(),done.number());
        f.processing().advance(ADMIN,0,1);var laterWork=new Processing.Work(jdbc.queryForObject("SELECT id FROM processamento_trabalhos WHERE lote_id=?",Long.class,running.number()),running.number(),running.remessaId(),running.scenario(),running.ug(),0,Processing.State.EM_PROCESSAMENTO,INITIAL,0);
        jdbc.update("UPDATE processamento_trabalhos SET devido_segundos=? WHERE lote_id=?",INITIAL.getEpochSecond()-1,done.number());var doneWork=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());tx.execute(()->repo.complete(doneWork,new StructuralRecords().process(done),INITIAL));
        var outside=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().advance(ADMIN,0,100);
        assertEquals(Processing.State.LOTE_DESCARTADO,f.processing().inspect(ADMIN,target.number()).state());for(var lot:List.of(running,outside))assertEquals(Processing.State.ABORTADO_ERRO_LOTE_ANTERIOR,f.processing().inspect(ADMIN,lot.number()).state());assertEquals(1,f.processing().inspect(ADMIN,done.number()).effective().size());
        assertFalse(tx.execute(()->repo.complete(laterWork,new StructuralRecords().process(target),INITIAL)));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));
    }
    static java.util.stream.Stream<Arguments> faults() {return java.util.stream.Stream.of("LOTE_DESCARTADO","ABORTADO_ERRO_LOTE_ANTERIOR").flatMap(event->java.util.stream.Stream.of(Arguments.of(event,false),Arguments.of(event,true)));}
    @ParameterizedTest @MethodSource("faults")
    void statementECommitRevertemGrupoInteiroAposExclusoes(String event,boolean commit) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var target=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,target.number(),0);f.processing().start(token,key);f.processing().advance(ADMIN,0,1);
        var jdbc=new JdbcTemplate(f.source());if(commit){jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN INSERT INTO falha_commit VALUES(-1); END");}else jdbc.execute("CREATE TRIGGER impedir BEFORE INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN SELECT RAISE(ABORT,'falha ficticia'); END");
        var before=snapshot(jdbc);assertThrows(RuntimeException.class,()->f.processing().advance(ADMIN,0,1));assertEquals(before,snapshot(jdbc));jdbc.execute("DROP TRIGGER impedir");assertEquals(1,f.processing().advance(ADMIN,0,100).passosExecutados());assertFalse(f.processing().inspect(ADMIN,target.number()).active());
    }
    javax.sql.DataSource legacyNine() throws Exception {
        var h=helper();var source=h.legacyEight();var resource=new ClassPathResource("db/migration/V9__finalizacao.sql");try(var c=source.getConnection()){ScriptUtils.executeSqlScript(c,resource);}new JdbcTemplate(source).update("INSERT INTO schema_migrations VALUES(9,?)",CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));return source;
    }
    @Test void v9PreenchidaPreservaTabelasJobsFechadosSequenciasEChecagens() throws Exception {
        var h=helper();var source=legacyNine();var jdbc=new JdbcTemplate(source);jdbc.update("UPDATE remessas SET finalizada_em=?",INITIAL.toString());var before=snapshot(jdbc);
        h.fixture("cenario-a");assertEquals(14,jdbc.queryForObject("PRAGMA user_version",Integer.class));var after=snapshot(jdbc);JdbcFinalizationTest.removeExpirationMigration(after);assertTrue(after.remove("competencias_bloqueadas").isEmpty());after.get("schema_migrations").removeLast();after.remove("falhas_processamento");after.remove("falhas_recepcao");after.get("schema_migrations").removeLast();after.get("schema_migrations").removeLast();after.get("schema_migrations").removeLast();for(var row:after.get("remessas"))assertNull(row.remove("cancelada_em"));for(var row:after.get("lotes"))assertEquals(1,row.remove("ativo"));for(var row:after.get("processamento_trabalhos")){assertNull(row.remove("codigo"));assertNull(row.remove("mensagem"));assertNull(row.remove("registro"));}assertEquals(before,after);
        var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing().start(token,key);assertTrue(jdbc.queryForObject("SELECT id FROM processamento_trabalhos WHERE lote_id=?",Long.class,lot.number())>77);assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=10");var corrupt=snapshot(jdbc);assertThrows(IllegalStateException.class,()->SqliteDatabase.open(h.settings("cenario-a")));assertEquals(corrupt,snapshot(jdbc));
    }
    @Test void v10FalhaReverteAlteracaoReconstrucaoSequenciasEVersionamento() throws Exception {
        var h=helper();var source=legacyNine();var jdbc=new JdbcTemplate(source);jdbc.execute("CREATE VIEW falhas_processamento AS SELECT 1 AS conflito");var before=snapshot(jdbc);
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(h.settings("cenario-a")));assertEquals(9,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(before,snapshot(jdbc));assertFalse(jdbc.queryForList("PRAGMA table_info(lotes)").stream().anyMatch(row->row.get("name").equals("ativo")));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
        jdbc.execute("DROP VIEW falhas_processamento");assertNotNull(h.fixture("cenario-a"));
    }
    @Test void concorrenciaPlanoInicioETerminaisNaoReaplicam() throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.processing().configure(ADMIN,Processing.Mode.AUTOMATICO,0);var target=a.receive(token,key,CreditoHttpTest.PAYLOAD);a.receive(token,key,CreditoHttpTest.PAYLOAD);a.processing().start(token,key);var b=h.fixture("cenario-a");
        var result=race(()->{a.processing().planFailure(ADMIN,target.number(),0);return "configured";},()->{b.processing().advance(ADMIN,0,1);return "begun";});assertTrue(result.contains("begun"));
        if(result.contains("configured")) {race(()->{a.processing().advance(ADMIN,0,10);return "manual";},()->{for(int i=0;i<10;i++)b.processing().tick();return "automatic";});assertEquals(Processing.State.LOTE_DESCARTADO,a.processing().inspect(ADMIN,target.number()).state());}
        else {assertTrue(result.contains(RemessaFailure.Kind.CONFLICT));a.processing().advance(ADMIN,0,10);assertEquals(Processing.State.PROCESSADO_SUCESSO,a.processing().inspect(ADMIN,target.number()).state());}
        var jdbc=new JdbcTemplate(a.source());var before=snapshot(jdbc);assertEquals(0,b.processing().advance(ADMIN,0,100).passosExecutados());assertEquals(before,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void geracaoAnteriorNaoAplicaPlanoEAposSucessoOuFechamentoNaoConfigura() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var target=f.receive(token,key,RemessaHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,target.number(),0);
        var jdbc=new JdbcTemplate(f.source());jdbc.update("UPDATE scenarios SET generation=1 WHERE id='cenario-a'");assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->f.processing().failurePlan(ADMIN,target.number())).kind());f.processing().start(token,key);f.processing().advance(ADMIN,0,100);assertEquals(Processing.State.PROCESSADO_SUCESSO,f.processing().inspect(ADMIN,target.number()).state());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().planFailure(ADMIN,target.number(),0)).kind());f.remessas().finish(token,key,totals(1));assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().removeFailure(ADMIN,target.number())).kind());
    }

    @Test void manualAutomaticoConcorrentesConfirmamUmaFalhaEAbortosUmaVez() throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.processing().configure(ADMIN,Processing.Mode.AUTOMATICO,0);var target=a.receive(token,key,SaldoHttpTest.PAYLOAD);a.receive(token,key,SaldoHttpTest.PAYLOAD);a.processing().planFailure(ADMIN,target.number(),0);a.processing().start(token,key);var b=h.fixture("cenario-a");
        race(()->{a.processing().advance(ADMIN,0,100);return "manual";},()->{for(int i=0;i<100;i++)b.processing().tick();return "automatic";});
        var jdbc=new JdbcTemplate(a.source());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='LOTE_DESCARTADO'",Integer.class));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ABORTADO_ERRO_LOTE_ANTERIOR'",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void abortoDuranteCalculoRejeitaConclusaoTardiaDoSeguinte() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var target=f.receive(token,key,SaldoHttpTest.PAYLOAD);var later=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().planFailure(ADMIN,target.number(),0);f.processing().start(token,key);f.processing().advance(ADMIN,0,1);
        var jdbc=new JdbcTemplate(f.source());var repo=new JdbcProcessingRepository(f.source());var tx=new JdbcTransactions(f.source());var targetWork=tx.execute(()->repo.next("cenario-a",INITIAL).orElseThrow());jdbc.update("UPDATE processamento_trabalhos SET situacao='EM_PROCESSAMENTO',devido_segundos=? WHERE lote_id=?",INITIAL.getEpochSecond()-1,later.number());
        var inspection=new br.dev.luka.esfinge.cenarios.application.ScenarioService(new br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository(f.source()),"cenario-a",ADMIN,f.auth());
        var guarded=new br.dev.luka.esfinge.remessas.application.ProcessingService(f.auth(),inspection,f.remessas(),new JdbcRemessaRepository(f.source()),repo,tx,f.clock(),f.clock(),received->{var records=new StructuralRecords().process(received);assertTrue(tx.execute(()->repo.fail(targetWork,INITIAL)));return records;},"cenario-a",f.auth(),inspection);
        assertEquals(0,guarded.advance(ADMIN,0,1).passosExecutados());assertEquals(Processing.State.ABORTADO_ERRO_LOTE_ANTERIOR,guarded.inspect(ADMIN,later.number()).state());assertTrue(guarded.inspect(ADMIN,later.number()).effective().isEmpty());assertEquals(0,guarded.advance(ADMIN,0,100).passosExecutados());
    }
    @Test void migraHighwaterSemTrabalhoRestante() throws Exception {
        var h=helper();var source=legacyNine();var jdbc=new JdbcTemplate(source);jdbc.update("DELETE FROM processamento_trabalhos");assertEquals(77,jdbc.queryForObject("SELECT seq FROM sqlite_sequence WHERE name='processamento_trabalhos'",Integer.class));h.fixture("cenario-a");assertEquals(77,jdbc.queryForObject("SELECT seq FROM sqlite_sequence WHERE name='processamento_trabalhos'",Integer.class));
    }

}
