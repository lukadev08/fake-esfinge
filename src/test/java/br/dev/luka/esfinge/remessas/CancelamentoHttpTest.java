package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.*;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class,properties={"simulator.users[0].ugs=0001,0002","simulator.clock-mode=fixed"})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(OutputCaptureExtension.class)
class CancelamentoHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-cancelamento-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
    }
    String token(String ug) throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
            .param("codigoUg",ug).param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token,int month) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",Integer.toString(month))
            .param("cancelamentoAutomatico","NAO")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    long receive(String token,String key,String payload) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
            .contentType("application/json").content(payload)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    ResultActions start(String token,String key) throws Exception {return http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",token).param("chavePacote",key));}
    ResultActions advance(long millis,int steps) throws Exception {return http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",ADMIN).contentType("application/json")
        .content("{\"milissegundos\":"+millis+",\"passos\":"+steps+"}"));}
    ResultActions consult(String token,long id) throws Exception {return http.perform(get("/v6/tributario/consultarPorNumeroLote/"+id).header("auth_token",token));}
    JsonNode inspect(long id) throws Exception {return json.readTree(http.perform(get("/__simulator/lotes/"+id).header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());}
    ResultActions discard(String token,String key,String subject) throws Exception {return http.perform(post("/v6/tributario/descarteParcialPorAssunto").header("auth_token",token).param("chavePacote",key).param("assunto",subject));}
    ResultActions cancel(String token,String key) throws Exception {return http.perform(post("/servicosGerais/cancelarEnvioParcial").header("auth_token",token).param("chavePacote",key));}
    @Test void cancelaPendenteEReenviaIntegralmenteComNovaChave() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        start(token,key).andExpect(status().isOk());
        cancel(token,key).andExpect(status().isOk()).andExpect(content().string(""));
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ABORTADO"))
            .andExpect(jsonPath("$.retornoErros.erros[0].codigo").value("SIM_ENVIO_CANCELADO"))
            .andExpect(jsonPath("$.retornoErros.erros[0].registro").value(org.hamcrest.Matchers.nullValue()));
        var old=inspect(id);assertFalse(old.get("ativo").asBoolean());assertEquals(0,old.get("recebidos").asInt());assertEquals(1,old.get("recebidosHistoricos").asInt());
        assertArrayEquals(RemessaHttpTest.PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),old.get("bytesBase64").binaryValue());
        String next=open(token,202601);assertNotEquals(key,next);long fresh=receive(token,next,RemessaHttpTest.PAYLOAD);
        assertTrue(fresh>id);start(token,next).andExpect(status().isOk());advance(1000,10).andExpect(status().isOk());
        var body=json.createObjectNode().put("chavePacote",next);for(String total:FinalizacaoHttpTest.TOTALS)body.put(total,"0");body.put(FinalizacaoHttpTest.TOTALS[0],"1");
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(body.toString())).andExpect(status().isOk());
        assertEquals(1,inspect(fresh).get("efetivos").asInt());
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);cancel(token,next).andExpect(status().isConflict());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void transporteAutenticacaoEChavesIsoladasRecusamSemEfeitos() throws Exception {
        String token=token("0001"),other=token("0002"),key=open(token,202601);receive(token,key,SaldoHttpTest.PAYLOAD);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);String route="/servicosGerais/cancelarEnvioParcial";
        cancel("invalido",key).andExpect(status().isUnauthorized());http.perform(post(route).content("{}")).andExpect(status().isUnauthorized());
        http.perform(post(route).header("X-Simulator-Key",ADMIN).param("chavePacote",key)).andExpect(status().isUnauthorized());
        cancel(other,key).andExpect(status().isNotFound());cancel(token,"inexistente").andExpect(status().isNotFound());
        for(var request:java.util.List.of(post(route),post(route).param("chavePacote",""),post(route).param("chavePacote",key,key),post(route).param("chavePacote",key).param("extra","1"),post(route).param("chavePacote",key).content(" "),post(route).param("chavePacote",key).content("{}")))
            http.perform(request.header("auth_token",token)).andExpect(status().isBadRequest());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void chaveCanceladaRejeitaMutacoesEConsultaHistoricaNaoRenova() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);cancel(token,key).andExpect(status().isOk());
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        cancel(token,key).andExpect(status().isConflict());start(token,key).andExpect(status().isConflict());discard(token,key,"CADASTRO_CONTRIBUINTE").andExpect(status().isConflict());
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isConflict());
        var body=json.createObjectNode().put("chavePacote",key);for(String total:FinalizacaoHttpTest.TOTALS)body.put(total,"0");
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(body.toString())).andExpect(status().isConflict());
        http.perform(put("/__simulator/falhas/processamento/"+id).header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"registro\":null}")).andExpect(status().isConflict());
        http.perform(delete("/__simulator/falhas/processamento/"+id).header("X-Simulator-Key",ADMIN)).andExpect(status().isConflict());
        consult(token,id).andExpect(status().isOk());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        assertNull(jdbc.queryForObject("SELECT finalizada_em FROM remessas WHERE chave=?",String.class,key));assertNotNull(jdbc.queryForObject("SELECT cancelada_em FROM remessas WHERE chave=?",String.class,key));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaStatementOuCommitReverteFechamentoExclusoesControlesEEvento(boolean commit,CapturedOutput logs) throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,SaldoHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());advance(1000,10).andExpect(status().isOk());receive(token,key,RemessaHttpTest.PAYLOAD);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ENVIO_CANCELADO' BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");
        var before=JdbcFinalizationTest.snapshot(jdbc);String body=cancel(token,key).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));for(String secret:new String[]{token,key,"SELECT ","INSERT ","Exception","stackTrace"}){assertFalse(body.contains(secret));assertFalse(logs.getAll().contains(secret));}
        jdbc.execute("DROP TRIGGER impedir");cancel(token,key).andExpect(status().isOk());consult(token,id).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO")).andExpect(jsonPath("$.retornoErros").doesNotExist());assertEquals(0,inspect(id).get("efetivos").asInt());
    }
    @Test void tokenExpiradoRecusaENovoLoginCancelaEnvioSemLotes() throws Exception {
        String token=token("0001"),key=open(token,202601);advance(3600000,1).andExpect(status().isOk());var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        cancel(token,key).andExpect(status().isUnauthorized());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));cancel(token("0001"),key).andExpect(status().isOk()).andExpect(content().string(""));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));
    }
    @ParameterizedTest @EnumSource(br.dev.luka.esfinge.remessas.domain.Assunto.class)
    void seisAssuntosRecebidosGanhamHistoricoAbortado(br.dev.luka.esfinge.remessas.domain.Assunto subject) throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,JdbcSubjectDiscardTest.payload(subject));cancel(token,key).andExpect(status().isOk()).andExpect(content().string(""));
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ABORTADO")).andExpect(jsonPath("$.retornoErros.erros[0].codigo").value("SIM_ENVIO_CANCELADO")).andExpect(jsonPath("$.retornoErros.erros[0].mensagem").value("Envio cancelado no simulador")).andExpect(jsonPath("$.retornoErros.erros[0].registro").value(org.hamcrest.Matchers.nullValue()));
        var saved=inspect(id);assertFalse(saved.get("ativo").asBoolean());assertEquals(1,saved.get("recebidosHistoricos").asInt());assertEquals(0,saved.get("recebidos").asInt());
    }
}
