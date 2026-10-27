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
class ExpiracaoHttpTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-expiracao-").resolve("state.sqlite").toString();}catch(Exception failure){throw new IllegalStateException(failure);}});
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
    void advance(long millis) throws Exception {
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key","admin-local-ficticio").contentType("application/json").content("{\"milissegundos\":"+millis+",\"passos\":1}"))
            .andExpect(status().isOk());
    }
    long receive(String token,String key) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    @Test void avancarSetentaEDuasHorasExpiraRecebidoPreservaHistoricoEPermiteNovaChave() throws Exception {
        String token=token(),key=open(token);
        long lot=json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key","admin-local-ficticio").contentType("application/json").content("{\"milissegundos\":259200000,\"passos\":1}"))
            .andExpect(status().isOk());
        String fresh=token();
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",fresh)).andExpect(status().isOk())
            .andExpect(jsonPath("$.situacao").value("ABORTADO")).andExpect(jsonPath("$.retornoErros.erros[0].codigo").value("SIM_ENVIO_EXPIRADO"));
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(false)).andExpect(jsonPath("$.recebidos").value(0)).andExpect(jsonPath("$.recebidosHistoricos").value(1))
            .andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0));
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",fresh).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isConflict());
        assertNotEquals(key,open(fresh));
    }
    @Test void tokenTemLimiteInclusivoDeUmaHoraENovoLoginPermiteRepetirSemPerda() throws Exception {
        String token=token(),key=open(token);long lot=receive(token,key);advance(3599999);
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        advance(1);var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isUnauthorized());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        String fresh=token();assertTrue(receive(fresh,key)>lot);advance(1);
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true)).andExpect(jsonPath("$.recebidos").value(1));
    }
    @Test void consultaAntesDoPrazoRenovaMasInspecaoAdministrativaNaoRenovaNemProcessa() throws Exception {
        String token=token(),key=open(token);long lot=receive(token,key);advance(259199999);String fresh=token();
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",fresh)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(get("/__simulator/configuracao").header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk());
        http.perform(get("/__simulator/processamento").header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk());
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andExpect(jsonPath("$.processados").value(0));assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
        advance(1);assertNull(jdbc.queryForObject("SELECT expirada_em FROM remessas WHERE chave=?",String.class,key));advance(259199999);
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token())).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ABORTADO"));
    }
    @Test void falhaRealDeCommitNaExpiracaoRetorna500SemEncerramentoParcial() throws Exception {
        String token=token(),key=open(token);long lot=receive(token,key);var jdbc=new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='ENVIO_EXPIRADO' BEGIN INSERT INTO falha_commit VALUES(-1); END");
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key","admin-local-ficticio").contentType("application/json").content("{\"milissegundos\":259200000,\"passos\":1}"))
            .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO"));
        assertNull(jdbc.queryForObject("SELECT expirada_em FROM remessas WHERE chave=?",String.class,key));assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM falha_commit",Integer.class));
        http.perform(get("/__simulator/lotes/"+lot).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true)).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        jdbc.execute("DROP TRIGGER impedir");advance(0);
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+lot).header("auth_token",token())).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ABORTADO"));
    }
}
