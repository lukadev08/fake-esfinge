package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import br.dev.luka.esfinge.configuration.SqliteDatabase;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;

class JdbcReceptionFailureTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var helper=new JdbcFinalizationTest();helper.temporary=temporary;return helper;}
    @ParameterizedTest @EnumSource(Assunto.class)
    void perdaNosSeisAssuntosConservaOriginalReiniciaRecuperaEFinalizaSemDeduplicar(Assunto subject) throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key(),payload=JdbcSubjectDiscardTest.payload(subject);
        f.remessas().planReception(ADMIN,key,subject,Reception.Type.APOS_COMMIT,1);var interruption=assertThrows(Reception.Interrupted.class,()->f.receive(token,key,payload));assertEquals(Reception.Type.APOS_COMMIT,interruption.type());
        var jdbc=new JdbcTemplate(f.source());long lost=jdbc.queryForObject("SELECT numero FROM lotes",Long.class);var original=f.processing().inspect(ADMIN,lost);
        assertEquals(Processing.State.RECEBIDO,original.state());assertArrayEquals(payload.getBytes(StandardCharsets.UTF_8),original.received().lote().bytes());assertTrue(original.effective().isEmpty());assertTrue(original.processed().isEmpty());
        assertTrue(jdbc.queryForList("SELECT typeof(valor) AS tipo FROM valores_recebidos").stream().allMatch(row->row.get("tipo").equals("text")));
        var before=snapshot(jdbc);var restarted=h.fixture("cenario-a");assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.RECOVERY_REQUIRED,assertThrows(RemessaFailure.class,()->restarted.receive(token,key,payload)).kind());assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->restarted.remessas().removeReceptionPlan(ADMIN,key,subject)).kind());
        restarted.processing().discard(token,key,subject);assertFalse(restarted.remessas().receptionPlan(ADMIN,key,subject).recuperacaoPendente());assertTrue(restarted.remessas().receptionPlan(ADMIN,key,subject).consumida());
        var first=restarted.receive(token,key,payload);var second=restarted.receive(token,key,payload);assertTrue(first.number()>lost);assertNotEquals(first.number(),second.number());assertEquals(original.received().lote().hash(),first.hash());
        h.complete(restarted,token,key);var totals=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var value:Assunto.values())totals.put(value,java.math.BigInteger.ZERO);totals.put(subject,java.math.BigInteger.TWO);restarted.remessas().finish(token,key,new Finalization(totals));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));assertFalse(restarted.processing().inspect(ADMIN,lost).active());assertArrayEquals(original.received().lote().bytes(),restarted.processing().inspect(ADMIN,lost).received().lote().bytes());
        var json=new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        assertEquals(json.readTree(payload).elements().next().get(0),json.readTree(restarted.processing().inspect(ADMIN,first.number()).effective().getFirst()));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @ParameterizedTest @EnumSource(Reception.Type.class)
    void alvoRelativoReinicioEConcorrenciaDeDuasInstanciasConsomemUmaVez(Reception.Type type) throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();
        a.receive(token,key,RemessaHttpTest.PAYLOAD);a.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,type,2);a.receive(token,key,RemessaHttpTest.PAYLOAD);
        var b=h.fixture("cenario-a");assertEquals(1,b.remessas().receptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE).tentativas());
        var results=race(()->attempt(a,token,key),()->attempt(b,token,key));assertEquals(1,results.stream().filter(type::equals).count());
        assertTrue(results.contains(type==Reception.Type.APOS_COMMIT?RemessaFailure.Kind.RECOVERY_REQUIRED:"received"));
        var jdbc=new JdbcTemplate(a.source());assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertEquals(2,jdbc.queryForObject("SELECT tentativas FROM falhas_recepcao",Integer.class));assertEquals(1,jdbc.queryForObject("SELECT consumida FROM falhas_recepcao",Integer.class));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    private Object attempt(Fixture fixture,String token,String key) {try {fixture.receive(token,key,RemessaHttpTest.PAYLOAD);return "received";}catch(Reception.Interrupted interruption){return interruption.type();}}
    @ParameterizedTest @CsvSource({"ANTES_ACEITACAO,false","ANTES_ACEITACAO,true","APOS_COMMIT,false","APOS_COMMIT,true"})
    void rollbackDeStatementECommitNaoConsomePlanoNemDeixaEfeitos(Reception.Type type,boolean commit) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,type,1);var jdbc=new JdbcTemplate(f.source());
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        String operation=type==Reception.Type.ANTES_ACEITACAO?"UPDATE ON falhas_recepcao WHEN NEW.consumida=1":"INSERT ON remessa_eventos WHEN NEW.tipo='RESPOSTA_LOTE_PERDIDA'";
        jdbc.execute("CREATE TRIGGER impedir AFTER "+operation+" BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");var before=snapshot(jdbc);
        RuntimeException failure=assertThrows(RuntimeException.class,()->f.receive(token,key,RemessaHttpTest.PAYLOAD));assertFalse(failure instanceof Reception.Interrupted);assertEquals(before,snapshot(jdbc));
        jdbc.execute("DROP TRIGGER impedir");assertThrows(Reception.Interrupted.class,()->f.receive(token,key,RemessaHttpTest.PAYLOAD));assertEquals(1,jdbc.queryForObject("SELECT consumida FROM falhas_recepcao",Integer.class));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void descarteRevertidoConservaMarcaEOriginalMasCancelamentoFechaContexto(boolean commit) {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,1);assertThrows(Reception.Interrupted.class,()->f.receive(token,key,RemessaHttpTest.PAYLOAD));var jdbc=new JdbcTemplate(f.source());
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ASSUNTO_DESCARTADO_CADASTRO_CONTRIBUINTE' BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");
        var before=snapshot(jdbc);assertThrows(RuntimeException.class,()->f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE));assertEquals(before,snapshot(jdbc));assertTrue(f.remessas().receptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE).recuperacaoPendente());
        jdbc.execute("DROP TRIGGER impedir");f.processing().cancel(token,key);assertFalse(f.remessas().receptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE).recuperacaoPendente());assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.receive(token,key,RemessaHttpTest.PAYLOAD)).kind());
        String fresh=f.remessas().open(token,202601,"NAO").key();assertNotEquals(key,fresh);assertNotNull(f.receive(token,fresh,RemessaHttpTest.PAYLOAD));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='RESPOSTA_LOTE_PERDIDA'",Integer.class));
    }
    @Test void isolamentoDeAssuntoUgChaveCenarioEGeracaoNaoDependeDoHash() {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,1);
        String ug=a.token("0002"),ugKey=a.remessas().open(ug,202601,"NAO").key(),other=a.remessas().open(token,202602,"NAO").key();a.receive(ug,ugKey,RemessaHttpTest.PAYLOAD);a.receive(token,other,RemessaHttpTest.PAYLOAD);a.receive(token,key,PrazoHttpTest.PAYLOAD);
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.receive(ug,key,RemessaHttpTest.PAYLOAD)).kind());var b=h.fixture("cenario-b");String bt=b.token("0001");assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,1)).kind());
        assertEquals(0,a.remessas().receptionPlan(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE).tentativas());var jdbc=new JdbcTemplate(a.source());jdbc.update("UPDATE scenarios SET generation=1 WHERE id='cenario-a'");assertNotNull(a.receive(token,key,RemessaHttpTest.PAYLOAD));assertEquals(0,jdbc.queryForObject("SELECT tentativas FROM falhas_recepcao",Integer.class));
    }
    @Test void upgradeV11PreservaTabelasChecksumsSequenciasEReverteFalhaDaV12() throws Exception {
        var previous=new JdbcCancellationTest();previous.temporary=temporary;var source=previous.legacyTen();var jdbc=new JdbcTemplate(source);var migration=new ClassPathResource("db/migration/V11__cancelamento.sql");try(var connection=source.getConnection()){ScriptUtils.executeSqlScript(connection,migration);}jdbc.update("INSERT INTO schema_migrations VALUES(11,?)",CredentialHash.token(migration.getContentAsString(StandardCharsets.UTF_8)));
        jdbc.execute("CREATE VIEW falhas_recepcao AS SELECT 1 AS conflito");var before=snapshot(jdbc);var schema=jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name");assertThrows(IllegalStateException.class,()->SqliteDatabase.open(helper().settings("cenario-a")));assertEquals(11,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(before,snapshot(jdbc));assertEquals(schema,jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name"));
        jdbc.execute("DROP VIEW falhas_recepcao");before=snapshot(jdbc);helper().fixture("cenario-a");assertEquals(13,jdbc.queryForObject("PRAGMA user_version",Integer.class));var after=snapshot(jdbc);assertTrue(after.remove("competencias_bloqueadas").isEmpty());after.get("schema_migrations").removeLast();assertTrue(after.remove("falhas_recepcao").isEmpty());after.get("schema_migrations").removeLast();assertEquals(before,after);assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
}
