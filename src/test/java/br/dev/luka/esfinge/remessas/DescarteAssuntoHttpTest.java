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
class DescarteAssuntoHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-descarte-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
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
    @Test void descarteHttpRemoveDadosPreservaSucessoEPermiteReenvio() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        start(token,key).andExpect(status().isOk());advance(1000,10).andExpect(status().isOk());
        discard(token,key,"CADASTRO_CONTRIBUINTE").andExpect(status().isOk()).andExpect(content().string(""));
        var old=inspect(id);assertFalse(old.get("ativo").asBoolean());assertEquals(0,old.get("efetivos").asInt());
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO")).andExpect(jsonPath("$.retornoErros").doesNotExist());
        assertArrayEquals(RemessaHttpTest.PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),old.get("bytesBase64").binaryValue());
        long next=receive(token,key,RemessaHttpTest.PAYLOAD);assertNotEquals(id,next);start(token,key).andExpect(status().isOk());advance(1000,10).andExpect(status().isOk());assertEquals(1,inspect(next).get("efetivos").asInt());
    }

    void assertStable(JsonNode before,JsonNode after) {var a=before.deepCopy();var b=after.deepCopy();((com.fasterxml.jackson.databind.node.ObjectNode)a).remove("eventos");((com.fasterxml.jackson.databind.node.ObjectNode)b).remove("eventos");assertEquals(a,b);for(int i=0;i<before.get("eventos").size();i++)assertEquals(before.get("eventos").get(i),after.get("eventos").get(i));}
    void zeroLatency() throws Exception {http.perform(put("/__simulator/processamento").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}")).andExpect(status().isOk());}
    @Test void erroDescarteReenvioFinalizaSomenteAtivosPreservandoOutroAssunto() throws Exception {
        zeroLatency();String token=token("0001"),key=open(token,202601);
        long first=receive(token,key,RemessaHttpTest.PAYLOAD),target=receive(token,key,RemessaHttpTest.PAYLOAD),later=receive(token,key,RemessaHttpTest.PAYLOAD),other=receive(token,key,PrazoHttpTest.PAYLOAD);
        http.perform(put("/__simulator/falhas/processamento/"+target).header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"registro\":0}")).andExpect(status().isOk());
        start(token,key).andExpect(status().isOk());advance(0,20).andExpect(status().isOk());var error=inspect(target);var aborted=inspect(later);var untouched=inspect(other);
        discard(token,key,"CADASTRO_CONTRIBUINTE").andExpect(status().isOk()).andExpect(content().string(""));
        assertStable(error,inspect(target));assertStable(aborted,inspect(later));assertStable(untouched,inspect(other));assertFalse(inspect(first).get("ativo").asBoolean());
        long replacement=receive(token,key,RemessaHttpTest.PAYLOAD);assertTrue(replacement>other);start(token,key).andExpect(status().isOk());advance(0,20).andExpect(status().isOk());
        var body=json.createObjectNode().put("chavePacote",key);for(String total:FinalizacaoHttpTest.TOTALS)body.put(total,"0");body.put(FinalizacaoHttpTest.TOTALS[0],"1").put(FinalizacaoHttpTest.TOTALS[5],"1");
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(body.toString())).andExpect(status().isOk());
        consult(token,target).andExpect(jsonPath("$.situacao").value("LOTE_DESCARTADO")).andExpect(jsonPath("$.retornoErros.erros[0].codigo").value("SIM_ERRO_INJETADO"));
        discard(token,key,"CADASTRO_CONTRIBUINTE").andExpect(status().isConflict());
    }
    @Test void transporteAutenticacaoEIsolamentoRejeitamSemEfeitos() throws Exception {
        String token=token("0001"),other=token("0002"),key=open(token,202601);receive(token,key,SaldoHttpTest.PAYLOAD);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);String route="/v6/tributario/descarteParcialPorAssunto";
        discard("invalido",key,"SALDO_INICIAL_CREDITO_TRIBUTARIO").andExpect(status().isUnauthorized());
        http.perform(post(route).content("{}")).andExpect(status().isUnauthorized());
        discard(other,key,"SALDO_INICIAL_CREDITO_TRIBUTARIO").andExpect(status().isNotFound());discard(token,"inexistente","SALDO_INICIAL_CREDITO_TRIBUTARIO").andExpect(status().isNotFound());
        for(String subject:new String[]{"OUTRO"," cadastro_contribuinte","CADASTRO_CONTRIBUINTE ","cadastro_contribuinte"})discard(token,key,subject).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SIM_COBERTURA_AUSENTE"));
        for(var request:java.util.List.of(post(route).param("chavePacote",key),post(route).param("assunto","CADASTRO_CONTRIBUINTE"),post(route).param("chavePacote","").param("assunto","CADASTRO_CONTRIBUINTE"),post(route).param("chavePacote",key).param("assunto",""),post(route).param("chavePacote",key,key).param("assunto","CADASTRO_CONTRIBUINTE"),post(route).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE","CADASTRO_CONTRIBUINTE"),post(route).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE").param("extra","1"),post(route).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE").content(" ")))
            http.perform(request.header("auth_token",token)).andExpect(status().isBadRequest());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        http.perform(post("/v6/tributario/descartar-lote").header("auth_token",token)).andExpect(status().isUnprocessableEntity());
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaStatementOuCommitAposExclusoesReverteTudo(boolean commit,CapturedOutput logs) throws Exception {
        zeroLatency();String token=token("0001"),key=open(token,202601);long id=receive(token,key,SaldoHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());advance(0,10).andExpect(status().isOk());receive(token,key,SaldoHttpTest.PAYLOAD);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ASSUNTO_DESCARTADO_SALDO_INICIAL_CREDITO_TRIBUTARIO' BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");
        var before=JdbcFinalizationTest.snapshot(jdbc);String body=discard(token,key,"SALDO_INICIAL_CREDITO_TRIBUTARIO").andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));for(String secret:new String[]{token,key,"SELECT ","INSERT ","Exception","stackTrace"}){assertFalse(body.contains(secret));assertFalse(logs.getAll().contains(secret));}
        jdbc.execute("DROP TRIGGER impedir");discard(token,key,"SALDO_INICIAL_CREDITO_TRIBUTARIO").andExpect(status().isOk());assertEquals(0,inspect(id).get("efetivos").asInt());
    }
    @Test void tokenExpiradoRecusaAntesDeEfeitosENovoLoginPermiteDescarte() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);advance(3600000,1).andExpect(status().isOk());var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        discard(token,key,"CADASTRO_CONTRIBUINTE").andExpect(status().isUnauthorized());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        discard(token("0001"),key,"CADASTRO_CONTRIBUINTE").andExpect(status().isOk()).andExpect(content().string(""));assertFalse(inspect(id).get("ativo").asBoolean());
    }
    @ParameterizedTest @EnumSource(br.dev.luka.esfinge.remessas.domain.Assunto.class)
    void seisIdentificadoresHttpDescartamRecebidoSemTrabalhoERepetemVazio(br.dev.luka.esfinge.remessas.domain.Assunto subject) throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,JdbcSubjectDiscardTest.payload(subject));
        discard(token,key,subject.name()).andExpect(status().isOk()).andExpect(content().string(""));
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("LOTE_DESCARTADO")).andExpect(jsonPath("$.retornoErros.erros[0].codigo").value("SIM_DESCARTE_ASSUNTO")).andExpect(jsonPath("$.retornoErros.erros[0].registro").value(org.hamcrest.Matchers.nullValue()));
        var old=inspect(id);assertFalse(old.get("ativo").asBoolean());assertEquals(0,old.get("recebidos").asInt());assertEquals(1,old.get("recebidosHistoricos").asInt());
        discard(token,key,subject.name()).andExpect(status().isOk()).andExpect(content().string(""));assertStable(old,inspect(id));
    }
}
