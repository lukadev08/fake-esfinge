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
class FinalizacaoHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-finalizacao-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
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
    static final String[] TOTALS={"quantidadeCadastroContribuinte","quantidadeLancamentoCreditosTributarios","quantidadeDiarioGeralArrecadacao","quantidadeBaixaCreditosTributarios","quantidadeSaldoInicialCreditosTributarios","quantidadeInicioPrazoPrescricional","quantidadeCadastroImobiliario","quantidadeCadastroPropriedadeImobiliaria","quantidadeRevisaoValorLancamentoCreditos","quantidadeEstornoReceitaDiarioGeralArrecadacao","quantidadeCertidaoDividaAtiva","quantidadeCobrancaDividaAtiva","quantidadeSituacaoCobrancaDividaAtiva","quantidadeNovoNumeroInscricaoImobiliaria","quantidadeLancamentoCreditosTributariosOriginal","quantidadeNumeroCadastroImobiliarioLancamento","quantidadeRevisaoTipoCreditoTributario"};
    com.fasterxml.jackson.databind.node.ObjectNode body(String key,String count) {
        var body=json.createObjectNode().put("chavePacote",key);for(String total:TOTALS) body.put(total,"0");body.put(TOTALS[0],count);return body;
    }
    ResultActions finish(String token,JsonNode body) throws Exception {return http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(body.toString()));}
    @Test void finalizaAtomicamentePreservaHistoricoELiberaCompetencia() throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);
        start(token,key).andExpect(status().isOk());advance(0,1).andExpect(status().isOk());advance(1000,1).andExpect(status().isOk());
        var before=inspect(id);
        finish(token,body(key,"1")).andExpect(status().isOk()).andExpect(content().string(""));
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);
        assertNotNull(jdbc.queryForObject("SELECT finalizada_em FROM remessas WHERE chave=?",String.class,key));
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_FINALIZADO'",Integer.class));
        finish(token,body(key,"1")).andExpect(status().isConflict());
        start(token,key).andExpect(status().isConflict());
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isConflict());
        var events=inspect(id).get("eventos");var activity=jdbc.queryForObject("SELECT atividade FROM remessas WHERE chave=?",String.class,key);
        advance(1000,1).andExpect(status().isOk());consult(token,id).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
        assertEquals(events,inspect(id).get("eventos"));assertEquals(activity,jdbc.queryForObject("SELECT atividade FROM remessas WHERE chave=?",String.class,key));
        assertEquals(before.get("registrosEfetivos"),inspect(id).get("registrosEfetivos"));assertNotEquals(key,open(token,202601));
    }
    @Test void schemaCompletoRecusaAusentesNulosTiposFormatosExtrasECobertura() throws Exception {
        String token=token("0001"),key=open(token,202601);var original=body(key,"0");
        for(String field:TOTALS) {
            var missing=original.deepCopy();missing.remove(field);finish(token,missing).andExpect(status().isUnprocessableEntity());
            for(JsonNode invalid:java.util.List.of(json.nullNode(),json.getNodeFactory().numberNode(0),json.getNodeFactory().booleanNode(false),json.createArrayNode(),json.createObjectNode())) {
                var bad=original.deepCopy();bad.set(field,invalid);finish(token,bad).andExpect(status().isUnprocessableEntity());
            }
            for(String invalid:new String[]{"","-1","+1","1.0"," 0","0 ","١","０","1e0"}) finish(token,original.deepCopy().put(field,invalid)).andExpect(status().isUnprocessableEntity());
        }
        for(int i=6;i<TOTALS.length;i++) {
            finish(token,original.deepCopy().put(TOTALS[i],"1")).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SIM_COBERTURA_AUSENTE"));
            finish(token,original.deepCopy().put(TOTALS[i],"00")).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SIM_ESTRUTURA_INVALIDA"));
        }
        for(JsonNode invalid:java.util.List.of(json.nullNode(),json.getNodeFactory().numberNode(1),json.getNodeFactory().booleanNode(true))) {var bad=original.deepCopy();bad.set("chavePacote",invalid);finish(token,bad).andExpect(status().isUnprocessableEntity());}
        finish(token,original.deepCopy().put("chavePacote","")).andExpect(status().isUnprocessableEntity());
        var missing=original.deepCopy();missing.remove("chavePacote");finish(token,missing).andExpect(status().isUnprocessableEntity());
        finish(token,original.deepCopy().put("extra","0")).andExpect(status().isUnprocessableEntity());
        finish(token,json.createArrayNode()).andExpect(status().isUnprocessableEntity());finish(token,json.nullNode()).andExpect(status().isUnprocessableEntity());
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos",Integer.class));assertNull(jdbc.queryForObject("SELECT finalizada_em FROM remessas WHERE chave=?",String.class,key));
    }
    @Test void transporteEstritoEAutenticacaoPrecedemEfeitos() throws Exception {
        String token=token("0001"),key=open(token,202601),valid=body(key,"0").toString();
        for(String bad:new String[]{"","{",valid+" {}",valid.replace("\"chavePacote\":","\"chavePacote\":\"duplicada\",\"chavePacote\":")})
            http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(bad)).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(new byte[]{(byte)0xc3,0x28})).andExpect(status().isBadRequest());
        for(String type:new String[]{"text/plain","application/json;charset=ISO-8859-1","application/xml"})
            http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType(type).content(valid)).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).content(valid)).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(valid)).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/finalizarEnvio?extra=x").header("auth_token",token).contentType("application/json").content(valid)).andExpect(status().isBadRequest());
        finish("invalido",body(key,"0")).andExpect(status().isUnauthorized());
        http.perform(post("/v6/tributario/finalizarEnvio").header("X-Simulator-Key",ADMIN).contentType("application/json").content(valid)).andExpect(status().isUnauthorized());
        finish(token("0002"),body(key,"0")).andExpect(status().isNotFound());finish(token,body("inexistente","0")).andExpect(status().isNotFound());
        advance(3600000,1).andExpect(status().isOk());finish(token,body(key,"0")).andExpect(status().isUnauthorized());
        assertEquals(1,new org.springframework.jdbc.core.JdbcTemplate(source).queryForObject("SELECT COUNT(*) FROM remessa_eventos",Integer.class));
    }
    @Test void pendenciasEDivergenciaNaoAlteramEnvioNemTempo() throws Exception {
        String token=token("0001"),key=open(token,202601);finish(token,body(key,"0")).andExpect(status().isConflict());
        long first=receive(token,key,RemessaHttpTest.PAYLOAD);var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);
        for(int stage=0;stage<4;stage++) {
            var events=inspect(first).get("eventos");var activity=jdbc.queryForObject("SELECT atividade FROM remessas WHERE chave=?",String.class,key);
            finish(token,body(key,"1")).andExpect(status().isConflict());assertEquals(events,inspect(first).get("eventos"));assertEquals(activity,jdbc.queryForObject("SELECT atividade FROM remessas WHERE chave=?",String.class,key));
            if(stage==0) start(token,key).andExpect(status().isOk());
            if(stage==1) advance(0,1).andExpect(status().isOk());
            if(stage==2) {receive(token,key,RemessaHttpTest.PAYLOAD);advance(1000,1).andExpect(status().isOk());}
        }
        start(token,key).andExpect(status().isOk());advance(0,1).andExpect(status().isOk());advance(1000,1).andExpect(status().isOk());
        for(String count:new String[]{"0","1","3","999999999999999999999999999999999999999999999999999999999999"}) finish(token,body(key,count)).andExpect(status().isConflict());
        finish(token,body(key,"0002")).andExpect(status().isOk());
    }
    @Test void seisAssuntosContamLinhasEfetivasComMultiplicidadeEIsolamento() throws Exception {
        http.perform(put("/__simulator/processamento").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}")).andExpect(status().isOk());
        String token=token("0001"),key=open(token,202601);var totals=body(key,"0");
        String[] fixtures={RemessaHttpTest.PAYLOAD,CreditoHttpTest.PAYLOAD,ArrecadacaoHttpTest.PAYLOAD,BaixaHttpTest.PAYLOAD,SaldoHttpTest.PAYLOAD,PrazoHttpTest.PAYLOAD};
        for(int i=0;i<fixtures.length;i++) {
            var root=(com.fasterxml.jackson.databind.node.ObjectNode)json.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(fixtures[i]);String list=root.fieldNames().next();root.withArray(list).add(root.get(list).get(0).deepCopy());
            receive(token,key,root.toString());receive(token,key,fixtures[i]);start(token,key).andExpect(status().isOk());advance(0,4).andExpect(status().isOk());totals.put(TOTALS[i],"0003");
        }
        String other=open(token,202602),otherToken=token("0002"),otherUg=open(otherToken,202601);receive(token,other,RemessaHttpTest.PAYLOAD);receive(otherToken,otherUg,RemessaHttpTest.PAYLOAD);
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);jdbc.update("UPDATE lotes SET registros=777 WHERE remessa_id=(SELECT id FROM remessas WHERE chave=?)",key);
        finish(token,totals).andExpect(status().isOk());assertEquals(18,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM remessas WHERE finalizada_em IS NULL",Integer.class));
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhasDeStatementECommitRetornam500EPermitemRetomada(boolean commit,CapturedOutput logs) throws Exception {
        String token=token("0001"),key=open(token,202601);long id=receive(token,key,RemessaHttpTest.PAYLOAD);start(token,key).andExpect(status().isOk());advance(0,1).andExpect(status().isOk());advance(1000,1).andExpect(status().isOk());
        var jdbc=new org.springframework.jdbc.core.JdbcTemplate(source);var before=inspect(id);var row=jdbc.queryForList("SELECT * FROM remessas");
        if(commit) {jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ENVIO_FINALIZADO' BEGIN INSERT INTO falha_commit VALUES(-1); END");}
        else jdbc.execute("CREATE TRIGGER impedir BEFORE INSERT ON remessa_eventos WHEN NEW.tipo='ENVIO_FINALIZADO' BEGIN SELECT RAISE(ABORT,'falha ficticia'); END");
        String response=finish(token,body(key,"1")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();
        for(String secret:new String[]{token,key,"SELECT ","INSERT ","Exception","stackTrace"}) {assertFalse(response.contains(secret));assertFalse(logs.getAll().contains(secret));}
        assertEquals(before,inspect(id));assertEquals(row,jdbc.queryForList("SELECT * FROM remessas"));if(commit) assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM falha_commit",Integer.class));
        jdbc.execute("DROP TRIGGER impedir");finish(token,body(key,"1")).andExpect(status().isOk());
    }
}
