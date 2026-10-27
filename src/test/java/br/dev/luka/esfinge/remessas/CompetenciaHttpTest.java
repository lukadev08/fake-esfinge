package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.*;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class,properties={"simulator.users[0].ugs=0001,0002","simulator.clock-mode=fixed"})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(OutputCaptureExtension.class)
class CompetenciaHttpTest {
    static final String ADMIN="admin-local-ficticio", ROUTE="/__simulator/competencias/0001/202601";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired javax.sql.DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-competencia-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
    }
    String token(String ug) throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
            .param("codigoUg",ug).param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token,int month) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",Integer.toString(month)).param("cancelamentoAutomatico","NAO"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    long receive(String token,String key) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    ResultActions block(String route,boolean blocked) throws Exception {
        return http.perform(put(route).header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"bloqueada\":"+blocked+"}"));
    }
    String totals(String key) {
        var body=json.createObjectNode().put("chavePacote",key);for(String total:FinalizacaoHttpTest.TOTALS)body.put(total,"0");body.put(FinalizacaoHttpTest.TOTALS[0],"1");return body.toString();
    }
    @Test void bloqueiaTodasMutacoesSemEfeitosEPermiteConsultaEDesbloqueio() throws Exception {
        String token=token("0001"),key=open(token,202601);long lot=receive(token,key);
        http.perform(put("/__simulator/falhas/recepcao").header("X-Simulator-Key",ADMIN).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE")
            .contentType("application/json").content("{\"tipo\":\"APOS_COMMIT\",\"tentativa\":2}")).andExpect(status().isOk());
        block(ROUTE,true).andExpect(status().isOk()).andExpect(jsonPath("$.ug").value("0001")).andExpect(jsonPath("$.anoMes").value(202601)).andExpect(jsonPath("$.bloqueada").value(true));
        var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        for(var request:java.util.List.of(
            get("/v6/tributario/iniciarEnvio").param("anoMes","202601").param("cancelamentoAutomatico","NAO"),
            post("/v6/tributario/enviarParcialLote").param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD),
            post("/v6/tributario/iniciar-processamento").param("chavePacote",key),
            post("/v6/tributario/finalizarEnvio").contentType("application/json").content(totals(key)),
            post("/v6/tributario/descarteParcialPorAssunto").param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE"),
            post("/servicosGerais/cancelarEnvioParcial").param("chavePacote",key))) {
            http.perform(request.header("auth_token",token)).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("SIM_ACESSO_NEGADO"));
            assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        }
        http.perform(get(ROUTE).header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.bloqueada").value(true));assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT tentativas FROM falhas_recepcao",Integer.class));
        block(ROUTE,false).andExpect(status().isOk()).andExpect(jsonPath("$.bloqueada").value(false));assertTrue(receive(token,key)>lot);
    }
    @Test void administracaoExigeChavePropriaEEntradaEstritaSemNormalizarUg() throws Exception {
        String token=token("0001");var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(get(ROUTE)).andExpect(status().isUnauthorized());
        http.perform(get(ROUTE).header("auth_token",token)).andExpect(status().isUnauthorized());
        http.perform(put(ROUTE).header("X-Simulator-Key",token).contentType("application/json").content("{\"bloqueada\":true}")).andExpect(status().isUnauthorized());
        http.perform(put("/__simulator/competencias/9999/invalido").content("{}")).andExpect(status().isUnauthorized());
        http.perform(get(ROUTE).header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.ug").value("0001")).andExpect(jsonPath("$.bloqueada").value(false));
        for(String ug:new String[]{"1","00001","9999"}) http.perform(get("/__simulator/competencias/"+ug+"/202601").header("X-Simulator-Key",ADMIN)).andExpect(status().isNotFound());
        for(String month:new String[]{"202600","202613","202701","0202601","+202601","２０２６０１","202601.0"})
            http.perform(get("/__simulator/competencias/0001/"+month).header("X-Simulator-Key",ADMIN)).andExpect(status().isBadRequest());
        for(String body:new String[]{"","null","{}","[]","{\"bloqueada\":null}","{\"bloqueada\":1}","{\"bloqueada\":\"true\"}","{\"bloqueada\":true,\"extra\":0}","{\"bloqueada\":true,\"bloqueada\":false}","{\"bloqueada\":true} {}"})
            http.perform(put(ROUTE).header("X-Simulator-Key",ADMIN).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        http.perform(get(ROUTE).header("X-Simulator-Key",ADMIN).content(" ")).andExpect(status().isBadRequest());
        http.perform(get(ROUTE).header("X-Simulator-Key",ADMIN).param("extra","1")).andExpect(status().isBadRequest());
        http.perform(put(ROUTE).header("X-Simulator-Key",ADMIN).param("extra","1").contentType("application/json").content("{\"bloqueada\":true}")).andExpect(status().isBadRequest());
        for(String media:new String[]{"text/plain","application/json;charset=ISO-8859-1"})
            http.perform(put(ROUTE).header("X-Simulator-Key",ADMIN).contentType(media).content("{\"bloqueada\":true}")).andExpect(status().isBadRequest());
        http.perform(put(ROUTE).header("X-Simulator-Key",ADMIN).contentType("application/json").content(new byte[]{(byte)0xc3,0x28})).andExpect(status().isBadRequest());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void isolamentoEPrecedenciaPreservamOutrasUgsMesesEChaveEncerrada() throws Exception {
        String token=token("0001"),other=token("0002"),key=open(token,202601);receive(token,key);
        http.perform(post("/servicosGerais/cancelarEnvioParcial").header("auth_token",token).param("chavePacote",key)).andExpect(status().isOk());
        block(ROUTE,true).andExpect(status().isOk());var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",other).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isNotFound());
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token","invalido").param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isUnauthorized());
        http.perform(post("/servicosGerais/cancelarEnvioParcial").header("auth_token",token).param("chavePacote",key)).andExpect(status().isForbidden());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));assertNotNull(open(other,202601));assertNotNull(open(token,202602));
        http.perform(get("/__simulator/competencias/0002/202601").header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.bloqueada").value(false));
        block(ROUTE,false).andExpect(status().isOk());
        http.perform(post("/servicosGerais/cancelarEnvioParcial").header("auth_token",token).param("chavePacote",key)).andExpect(status().isConflict());
    }
    @Test void trabalhoAceitoContinuaEAdministracaoPermaneceDisponivel() throws Exception {
        String token=token("0001"),key=open(token,202601);long lot=receive(token,key);
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",token).param("chavePacote",key)).andExpect(status().isOk());
        block(ROUTE,true).andExpect(status().isOk());
        http.perform(put("/__simulator/falhas/recepcao").header("X-Simulator-Key",ADMIN).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE")
            .contentType("application/json").content("{\"tipo\":\"ANTES_ACEITACAO\",\"tentativa\":1}")).andExpect(status().isOk());
        http.perform(delete("/__simulator/falhas/recepcao").header("X-Simulator-Key",ADMIN).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE")).andExpect(status().isOk());
        var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"milissegundos\":1000,\"passos\":10}")).andExpect(status().isOk());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROCESSADO_SUCESSO"));
        before=JdbcFinalizationTest.snapshot(jdbc);http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(totals(key))).andExpect(status().isForbidden());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        block(ROUTE,false).andExpect(status().isOk());http.perform(post("/v6/tributario/finalizarEnvio").header("auth_token",token).contentType("application/json").content(totals(key))).andExpect(status().isOk());
    }
    @ParameterizedTest @ValueSource(booleans={false,true})
    void falhaDeStatementOuCommitNaoConfirmaBloqueioNemExpoeDetalhes(boolean commit,CapturedOutput logs) throws Exception {
        String token=token("0001");var jdbc=new JdbcTemplate(source);
        if(commit)jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON competencias_bloqueadas BEGIN "+(commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");var before=JdbcFinalizationTest.snapshot(jdbc);
        String body=block(ROUTE,true).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        for(String secret:new String[]{token,"SELECT ","INSERT ","Exception","stackTrace"}){assertFalse(body.contains(secret));assertFalse(logs.getAll().contains(secret));}
        jdbc.execute("DROP TRIGGER impedir");block(ROUTE,true).andExpect(status().isOk());
    }
}
