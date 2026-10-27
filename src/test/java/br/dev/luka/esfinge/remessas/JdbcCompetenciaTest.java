package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import br.dev.luka.esfinge.configuration.SqliteDatabase;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;

class JdbcCompetenciaTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var helper=new JdbcFinalizationTest();helper.temporary=temporary;return helper;}
    @Test void bloqueioPersisteNoReinicioIsolaUgMesCenarioENaoConsomePlano() {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.receive(token,key,SaldoHttpTest.PAYLOAD);
        a.remessas().planReception(ADMIN,key,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,Reception.Type.APOS_COMMIT,1);a.remessas().blockCompetencia(ADMIN,"0001",202601,true);var jdbc=new JdbcTemplate(a.source());var before=snapshot(jdbc);
        var restarted=h.fixture("cenario-a");assertEquals(before,snapshot(jdbc));assertTrue(restarted.remessas().competencia(ADMIN,"0001",202601).bloqueada());assertEquals(before,snapshot(jdbc));
        assertEquals(RemessaFailure.Kind.BLOCKED,assertThrows(RemessaFailure.class,()->restarted.receive(token,key,SaldoHttpTest.PAYLOAD)).kind());assertEquals(before,snapshot(jdbc));assertEquals(0,restarted.remessas().receptionPlan(ADMIN,key,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO).tentativas());
        String other=a.token("0002");assertNotNull(a.remessas().open(other,202601,"NAO"));assertNotNull(a.remessas().open(token,202602,"NAO"));
        var b=h.fixture("cenario-b");String bt=b.token("0001");assertFalse(b.remessas().competencia(ADMIN,"0001",202601).bloqueada());assertNotNull(b.remessas().open(bt,202601,"NAO"));
        before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.receive(bt,key,SaldoHttpTest.PAYLOAD)).kind());assertEquals(before,snapshot(jdbc));
        restarted.remessas().blockCompetencia(ADMIN,"0001",202601,false);assertThrows(Reception.Interrupted.class,()->restarted.receive(token,key,SaldoHttpTest.PAYLOAD));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void corridaEntreBloqueioERecepcaoSerializaAceitacaoOuRecusaSemEfeitoParcial() throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();var b=h.fixture("cenario-a");
        a.remessas().planReception(ADMIN,key,Assunto.CADASTRO_CONTRIBUINTE,Reception.Type.APOS_COMMIT,2);
        var results=race(()->{a.receive(token,key,RemessaHttpTest.PAYLOAD);return "received";},()->{b.remessas().blockCompetencia(ADMIN,"0001",202601,true);return "blocked";});
        assertTrue(results.contains("blocked"));boolean received=results.contains("received");assertTrue(received || results.contains(RemessaFailure.Kind.BLOCKED));
        var jdbc=new JdbcTemplate(a.source());assertEquals(received?1:0,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertEquals(received?1:0,jdbc.queryForObject("SELECT tentativas FROM falhas_recepcao",Integer.class));assertEquals(received?1:0,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='LOTE_RECEBIDO'",Integer.class));
        var before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.BLOCKED,assertThrows(RemessaFailure.class,()->a.receive(token,key,RemessaHttpTest.PAYLOAD)).kind());assertEquals(before,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void bloqueioDuranteCalculoNaoCancelaTrabalhoPreviamenteAceito() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,CreditoHttpTest.PAYLOAD);f.processing().start(token,key);f.processing().advance(ADMIN,0,1);
        var inspection=new br.dev.luka.esfinge.cenarios.application.ScenarioService(new br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository(f.source()),"cenario-a",ADMIN,f.auth());
        var processor=new br.dev.luka.esfinge.remessas.application.ProcessingService(f.auth(),inspection,f.remessas(),new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository(f.source()),new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcProcessingRepository(f.source()),new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcTransactions(f.source()),f.clock(),f.clock(),received->{
            f.remessas().blockCompetencia(ADMIN,"0001",202601,true);return new br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords().process(received);
        },"cenario-a");
        assertEquals(1,processor.advance(ADMIN,0,1).passosExecutados());var snapshot=processor.inspect(ADMIN,lot.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,snapshot.state());assertEquals(1,snapshot.effective().size());assertArrayEquals(CreditoHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8),snapshot.received().lote().bytes());
        var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.BLOCKED,assertThrows(RemessaFailure.class,()->f.processing().cancel(token,key)).kind());assertEquals(before,snapshot(jdbc));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void desbloqueioComFalhaDeStatementOuCommitConservaBloqueio(boolean commit) {
        var h=helper();var f=h.fixture("cenario-a");f.remessas().blockCompetencia(ADMIN,"0001",202601,true);var jdbc=new JdbcTemplate(f.source());
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER DELETE ON competencias_bloqueadas BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");var before=snapshot(jdbc);
        assertThrows(RuntimeException.class,()->f.remessas().blockCompetencia(ADMIN,"0001",202601,false));assertEquals(before,snapshot(jdbc));assertTrue(f.remessas().competencia(ADMIN,"0001",202601).bloqueada());
        jdbc.execute("DROP TRIGGER impedir");assertFalse(f.remessas().blockCompetencia(ADMIN,"0001",202601,false).bloqueada());
    }
    @Test void upgradeV12PreenchidaPreservaConteudoControlesChecksumsEFalhaReverteSchema() throws Exception {
        var old=new JdbcCancellationTest();old.temporary=temporary;var source=old.legacyTen();var jdbc=new JdbcTemplate(source);
        for(String script:List.of("V11__cancelamento.sql","V12__falhas_recepcao.sql")) {
            var resource=new ClassPathResource("db/migration/"+script);try(var connection=source.getConnection()){ScriptUtils.executeSqlScript(connection,resource);}
            jdbc.update("INSERT INTO schema_migrations VALUES(?,?)",script.startsWith("V11")?11:12,CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        }
        var remit=jdbc.queryForMap("SELECT id,cenario,ug FROM remessas LIMIT 1");jdbc.update("INSERT INTO falhas_recepcao(remessa_id,cenario,ug,geracao,assunto,tipo,tentativa,tentativas,consumida,recuperacao) VALUES(?,?,?,0,'CADASTRO_CONTRIBUINTE','APOS_COMMIT',3,3,1,1)",remit.get("id"),remit.get("cenario"),remit.get("ug"));
        jdbc.execute("CREATE VIEW competencias_bloqueadas AS SELECT 1 AS conflito");var before=snapshot(jdbc);var schema=jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name");
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(helper().settings("cenario-a")));assertEquals(12,jdbc.queryForObject("PRAGMA user_version",Integer.class));assertEquals(before,snapshot(jdbc));assertEquals(schema,jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name"));
        jdbc.execute("DROP VIEW competencias_bloqueadas");before=snapshot(jdbc);var migrated=helper().fixture("cenario-a");assertEquals(14,jdbc.queryForObject("PRAGMA user_version",Integer.class));var after=snapshot(jdbc);JdbcFinalizationTest.removeExpirationMigration(after);assertTrue(after.remove("competencias_bloqueadas").isEmpty());after.get("schema_migrations").removeLast();assertEquals(before,after);
        migrated.remessas().blockCompetencia(ADMIN,"0001",202601,true);var saved=snapshot(jdbc);helper().fixture("cenario-a");assertEquals(saved,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
}
