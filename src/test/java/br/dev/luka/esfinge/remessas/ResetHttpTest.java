package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class,properties={"simulator.clock-mode=fixed"})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ResetHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-reset-").resolve("state.sqlite").toString();}catch(Exception failure){throw new IllegalStateException(failure);}});
    }
    String token() throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
            .param("codigoUg","0001").param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes","202601").param("cancelamentoAutomatico","NAO"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    long receive(String token,String key) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    @Test void resetInvalidaSessaoTrabalhoEHistoricoEPermiteRepetirComIdsNovos() throws Exception {
        String token=token(),key=open(token);long lot=receive(token,key);
        http.perform(put("/__simulator/processamento").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":12345}")).andExpect(status().isOk());
        http.perform(put("/__simulator/falhas/recepcao").header("X-Simulator-Key",ADMIN).param("chavePacote",key).param("assunto","CADASTRO_CONTRIBUINTE").contentType("application/json").content("{\"tipo\":\"APOS_COMMIT\",\"tentativa\":2}")).andExpect(status().isOk());
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",token).param("chavePacote",key)).andExpect(status().isOk());
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"milissegundos\":1000,\"passos\":1}")).andExpect(status().isOk());
        http.perform(put("/__simulator/competencias/0001/202601").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"bloqueada\":true}")).andExpect(status().isOk());
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.cenario").value("cenario-ficticio-a")).andExpect(jsonPath("$.geracaoAnterior").value(0)).andExpect(jsonPath("$.geracao").value(1))
            .andExpect(jsonPath("$.remessasRemovidas").value(1)).andExpect(jsonPath("$.lotesRemovidos").value(1)).andExpect(jsonPath("$.sessoesInvalidadas").value(1));
        var jdbc=new JdbcTemplate(source);
        for(String table:new String[]{"remessas","lotes","remessa_eventos","processamento_trabalhos","falhas_recepcao","competencias_bloqueadas","sessions"}) assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM "+table,Integer.class),table);
        http.perform(get("/__simulator/processamento").header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.modo").value("MANUAL"))
            .andExpect(jsonPath("$.latenciaMilissegundos").value(1000)).andExpect(jsonPath("$.instante").value("2026-01-01T00:00:00Z"));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key",ADMIN)).andExpect(status().isNotFound());
        String fresh=token(),newKey=open(fresh);assertNotEquals(key,newKey);assertTrue(receive(fresh,newKey)>lot);
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}"))
            .andExpect(status().isConflict());assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessas",Integer.class));
    }
    @Test void administracaoEEntradaEstritasNaoAlteramNenhumaTabela() throws Exception {
        String token=token();open(token);var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        for(String key:new String[]{"",token,"chave-incorreta"}) http.perform(post("/__simulator/reset").header("X-Simulator-Key",key).contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
        for(String body:new String[]{"","null","[]","{}","{\"cenario\":\"cenario-ficticio-a\"}","{\"cenario\":null,\"geracaoEsperada\":0}","{\"cenario\":\"\",\"geracaoEsperada\":0}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":-1}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":1.0}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":\"0\"}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":true}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":9223372036854775808}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0,\"extra\":1}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0,\"geracaoEsperada\":0}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0} {}"})
            http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        String valid="{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}";
        http.perform(post("/__simulator/reset?extra=1").header("X-Simulator-Key",ADMIN).contentType("application/json").content(valid)).andExpect(status().isBadRequest());
        for(String media:new String[]{"text/plain","application/json;charset=ISO-8859-1"}) http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType(media).content(valid)).andExpect(status().isBadRequest());
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content(new byte[]{(byte)0xc3,0x28})).andExpect(status().isBadRequest());
        for(String body:new String[]{"{\"cenario\":\"outro-cenario\",\"geracaoEsperada\":0}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":1}","{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":9223372036854775807}"})
            http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content(body)).andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("SIM_ESTADO_INVALIDO"));
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void falhaRealDeCommitDepoisDosParticipantesRetorna500SemResetParcial() throws Exception {
        String token=token(),key=open(token);receive(token,key);var jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE falha_commit(id TEXT REFERENCES scenarios(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER DELETE ON sessions BEGIN INSERT INTO falha_commit VALUES('cenario-inexistente'); END");var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}"))
            .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO"));
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/1").header("auth_token",token)).andExpect(status().isOk());
        jdbc.execute("DROP TRIGGER impedir");
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.geracao").value(1));
    }
    @Test void resetDeCenarioVazioTambemAvancaGeracaoSemCriarDados() throws Exception {
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.geracao").value(1)).andExpect(jsonPath("$.remessasRemovidas").value(0)).andExpect(jsonPath("$.lotesRemovidos").value(0)).andExpect(jsonPath("$.sessoesInvalidadas").value(0));
        String token=token();assertNotNull(open(token));
    }
}
