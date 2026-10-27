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
class ProcessamentoHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-processamento-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
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
    @Test void disparoEAvancoManualPersistemEstadosERegistrosSemConsultaProcessar() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        var started=json.readTree(start(token,key).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO")).andReturn().getResponse().getContentAsString());
        assertEquals(3,started.size());assertTrue(started.get("identificadorRemessaOnline").isIntegralNumber());assertTrue(started.has("dataEnvio"));
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        advance(0,1).andExpect(status().isOk()).andExpect(jsonPath("$.passosExecutados").value(1));
        for(int i=0;i<3;i++) consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("EM_PROCESSAMENTO"));
        assertEquals(0,inspect(id).get("processados").asInt());
        advance(1000,1).andExpect(status().isOk()).andExpect(jsonPath("$.passosExecutados").value(1));
        consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
        var saved=inspect(id);assertEquals(1,saved.get("recebidos").asInt());assertEquals(1,saved.get("processados").asInt());assertEquals(1,saved.get("efetivos").asInt());
        assertEquals(saved.get("registros"),saved.get("registrosProcessados"));assertEquals(saved.get("registros"),saved.get("registrosEfetivos"));
        assertArrayEquals(RemessaHttpTest.PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),saved.get("bytesBase64").binaryValue());
    }
    ResultActions configure(String body) throws Exception {return http.perform(put("/__simulator/processamento").header("X-Simulator-Key",ADMIN).contentType("application/json").content(body));}
    @Test void snapshotNaoAgendaRecebidosPosterioresEConflitosNaoReaplicam() throws Exception {
        String token=token("0001"),key=open(token,202601);
        start(token,key).andExpect(status().isConflict());
        long first=receive(token,key,RemessaHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());
        long later=receive(token,key,RemessaHttpTest.PAYLOAD);int before=inspect(first).get("eventos").size();
        start(token,key).andExpect(status().isConflict());assertEquals(before,inspect(first).get("eventos").size());
        advance(0,1).andExpect(status().isOk());advance(1000,1).andExpect(status().isOk());
        consult(token,first).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
        consult(token,later).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        assertEquals(0,inspect(later).get("efetivos").asInt());
        start(token,key).andExpect(status().isOk());advance(0,1).andExpect(status().isOk());advance(1000,1).andExpect(status().isOk());
        start(token,key).andExpect(status().isConflict());advance(1000,10).andExpect(status().isOk()).andExpect(jsonPath("$.passosExecutados").value(0));
        assertEquals(1,inspect(first).get("efetivos").asInt());assertEquals(1,inspect(later).get("efetivos").asInt());
    }
    @Test void seisAssuntosEmCiclosPreservamMultiplicidadeValoresEReferencias() throws Exception {
        configure("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}").andExpect(status().isOk());
        String token=token("0001"),key=open(token,202601);
        var exact=json.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        for(String payload:new String[]{SaldoHttpTest.PAYLOAD,RemessaHttpTest.PAYLOAD,CreditoHttpTest.PAYLOAD,PrazoHttpTest.PAYLOAD,ArrecadacaoHttpTest.PAYLOAD,BaixaHttpTest.PAYLOAD}) {
            var root=(com.fasterxml.jackson.databind.node.ObjectNode)exact.readTree(payload);String list=root.fieldNames().next();
            root.withArray(list).add(root.get(list).get(0).deepCopy());String doubled=root.toString();
            long first=receive(token,key,doubled),second=receive(token,key,doubled);assertNotEquals(first,second);
            start(token,key).andExpect(status().isOk());advance(0,4).andExpect(status().isOk()).andExpect(jsonPath("$.passosExecutados").value(4));
            for(long id:new long[]{first,second}) {
                consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
                var snapshot=exact.readTree(http.perform(get("/__simulator/lotes/"+id).header("X-Simulator-Key",ADMIN)).andReturn().getResponse().getContentAsString());
                assertEquals(2,snapshot.get("processados").asInt());assertEquals(2,snapshot.get("efetivos").asInt());
                assertEquals(root.get(list),snapshot.get("registrosProcessados"));assertEquals(root.get(list),snapshot.get("registrosEfetivos"));
                assertArrayEquals(doubled.getBytes(java.nio.charset.StandardCharsets.UTF_8),snapshot.get("bytesBase64").binaryValue());
            }
        }
    }
    @Test void configuracaoNaoReagendaTrabalhoELeiturasNaoAvancamTempo() throws Exception {
        configure("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":17}").andExpect(status().isOk());
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());
        configure("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}").andExpect(status().isOk());
        advance(0,1).andExpect(status().isOk());
        for(int i=0;i<3;i++) {
            http.perform(get("/__simulator/processamento").header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.instante").value("2026-01-01T00:00:00Z"));
            consult(token,id).andExpect(jsonPath("$.situacao").value("EM_PROCESSAMENTO"));
        }
        advance(16,1).andExpect(jsonPath("$.passosExecutados").value(0));advance(1,1).andExpect(jsonPath("$.passosExecutados").value(1));
        consult(token,id).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
        long next=receive(token,key,RemessaHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());advance(0,2).andExpect(jsonPath("$.passosExecutados").value(2));
        consult(token,next).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
    }
    @Test void administracaoEstritaEIsolamentoNaoVazamOuMutam() throws Exception {
        String token=token("0001"),other=token("0002"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        start(other,key).andExpect(status().isNotFound());start("invalido",key).andExpect(status().isUnauthorized());
        for(String bad:new String[]{"{}","{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":\"1\"}","{\"modo\":0,\"latenciaMilissegundos\":1}","{\"modo\":\"OUTRO\",\"latenciaMilissegundos\":1}","{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":true}","{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":null}","{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":-1}","{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":1,\"extra\":0}","{\"modo\":\"MANUAL\",\"modo\":\"AUTOMATICO\",\"latenciaMilissegundos\":1}"}) configure(bad).andExpect(status().isBadRequest());
        for(String bad:new String[]{"{\"milissegundos\":\"1\",\"passos\":1}","{\"milissegundos\":true,\"passos\":1}","{\"milissegundos\":null,\"passos\":1}","{\"milissegundos\":-1,\"passos\":1}","{\"milissegundos\":1,\"passos\":0}","{\"milissegundos\":1,\"passos\":1.5}","{\"milissegundos\":1,\"passos\":1,\"extra\":0}"})
            http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",ADMIN).contentType("application/json").content(bad)).andExpect(status().isBadRequest());
        http.perform(get("/__simulator/processamento").header("auth_token",token)).andExpect(status().isUnauthorized());
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",token).contentType("application/json").content("{}")).andExpect(status().isUnauthorized());
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",token).param("chavePacote",key).content("{}")).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",token).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE")).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token","invalido").content("{}")).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/processamento").header("X-Simulator-Key",ADMIN)).andExpect(jsonPath("$.modo").value("MANUAL")).andExpect(jsonPath("$.latenciaMilissegundos").value(1000)).andExpect(jsonPath("$.instante").value("2026-01-01T00:00:00Z"));
        assertEquals(2,inspect(id).get("eventos").size());
    }
    @Test void automaticoExecutaPelaMesmaPortaSemConsultaDirigirProgresso() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        configure("{\"modo\":\"AUTOMATICO\",\"latenciaMilissegundos\":0}").andExpect(status().isOk());start(token,key).andExpect(status().isOk());
        org.awaitility.Awaitility.await().atMost(java.time.Duration.ofSeconds(5)).untilAsserted(()->assertEquals("PROCESSADO_SUCESSO",inspect(id).get("situacao").asText()));
        assertEquals(1,inspect(id).get("efetivos").asInt());
    }
    @Test void overflowDoAvancoRejeitaAntesDeAlterarRelogio() throws Exception {
        advance(Long.MAX_VALUE,1).andExpect(status().isBadRequest());
        var before=http.perform(get("/__simulator/processamento").header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        advance(Long.MAX_VALUE,1).andExpect(status().isBadRequest());
        assertEquals(before,http.perform(get("/__simulator/processamento").header("X-Simulator-Key",ADMIN)).andReturn().getResponse().getContentAsString());
        long boundary=java.time.Duration.between(java.time.Instant.parse("2026-01-01T00:00:00Z"),java.time.Instant.parse("9999-12-31T23:59:59.999Z")).toMillis();
        advance(boundary,1).andExpect(status().isOk()).andExpect(jsonPath("$.instante").value("9999-12-31T23:59:59.999Z"));
        advance(1,1).andExpect(status().isBadRequest());
        String token=token("0001"),key=open(token,202601);receive(token,key,RemessaHttpTest.PAYLOAD);
        configure("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}").andExpect(status().isOk());
        start(token,key).andExpect(status().isOk()).andExpect(jsonPath("$.dataEnvio").value("9999-12-31T23:59:59.999Z"));
    }
    static java.util.stream.Stream<Arguments> failures() {return java.util.stream.Stream.of("PROCESSAMENTO_DISPARADO","LOTE_EM_PROCESSAMENTO","LOTE_PROCESSADO_SUCESSO").flatMap(type->java.util.stream.Stream.of(Arguments.of(type,false),Arguments.of(type,true)));}
    @ParameterizedTest @MethodSource("failures")
    void falhasReaisDePersistenciaRetornam500SemConfirmarOuExpor(String event,boolean commit,CapturedOutput logs) throws Exception {
        configure("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}").andExpect(status().isOk());
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,SaldoHttpTest.PAYLOAD);
        if(!event.equals("PROCESSAMENTO_DISPARADO")) start(token,key).andExpect(status().isOk());
        if(event.equals("LOTE_PROCESSADO_SUCESSO")) advance(0,1).andExpect(status().isOk());
        var before=inspect(id);var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);
        if(commit) {jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN INSERT INTO falha_commit VALUES(-1); END");}
        else jdbc.execute("CREATE TRIGGER impedir BEFORE INSERT ON remessa_eventos WHEN NEW.tipo='"+event+"' BEGIN SELECT RAISE(ABORT,'falha ficticia'); END");
        var response=event.equals("PROCESSAMENTO_DISPARADO")?start(token,key):advance(0,1);
        String body=response.andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();
        for(String secret:new String[]{token,key,"senha-ficticia-a","SELECT ","INSERT ","Exception","stackTrace"}) {assertFalse(body.contains(secret));assertFalse(logs.getAll().contains(secret));}
        var failed=inspect(id);assertEquals(before.get("situacao"),failed.get("situacao"));assertEquals(before.get("eventos"),failed.get("eventos"));assertEquals(0,failed.get("processados").asInt());assertEquals(0,failed.get("efetivos").asInt());
        jdbc.execute("DROP TRIGGER impedir");if(event.equals("PROCESSAMENTO_DISPARADO")) start(token,key).andExpect(status().isOk());advance(0,10).andExpect(status().isOk());assertEquals(1,inspect(id).get("efetivos").asInt());
    }
}
