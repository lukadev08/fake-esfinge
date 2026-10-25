package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import javax.sql.DataSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.system.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class)
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@ExtendWith(OutputCaptureExtension.class)
class RemessaFailureHttpTest {
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-commit-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
    }
    static java.util.stream.Stream<Arguments> failures() {
        return java.util.stream.IntStream.range(0,3).boxed().flatMap(subject->java.util.stream.Stream.of(Arguments.of(false,subject),Arguments.of(true,subject)));
    }
    @ParameterizedTest @MethodSource("failures")
    void falhaStatementOuCommitNaoConfirmaESanitiza(boolean commit,int subject,CapturedOutput logs) throws Exception {
        String payload=switch(subject) {case 0 -> RemessaHttpTest.PAYLOAD;case 1 -> CreditoHttpTest.PAYLOAD;default -> ArrecadacaoHttpTest.PAYLOAD;};
        String token=json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
                .param("codigoUg","0001").param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
        String key=json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",Integer.toString(202601+(commit?1:0)+subject*2))
                .param("cancelamentoAutomatico","NAO")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
        var jdbc=new JdbcTemplate(source);
        int valuesBefore=jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class);
        jdbc.execute("CREATE TABLE IF NOT EXISTS falha_commit(remessa_id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");
        jdbc.execute("CREATE TRIGGER impedir_confirmacao AFTER INSERT ON remessa_eventos WHEN NEW.tipo='LOTE_RECEBIDO' BEGIN "+
                (commit?"INSERT INTO falha_commit VALUES(-1);":"SELECT RAISE(ABORT,'falha ficticia');")+" END");
        String error;
        try {
            error=http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
                    .contentType("application/json").content(payload)).andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO")).andReturn().getResponse().getContentAsString();
        } finally { jdbc.execute("DROP TRIGGER impedir_confirmacao"); }
        assertEquals(valuesBefore,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        for(String secret:new String[]{token,"senha-ficticia-a","SELECT ","INSERT ","Exception","Pessoa Ficticia"}) {
            assertFalse(error.contains(secret));assertFalse(logs.getAll().contains(secret));
        }
        long number=json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
                .contentType("application/json").content(payload)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        assertEquals(valuesBefore+(subject==0?0:1),jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.eventos.length()").value(2)).andExpect(jsonPath("$.recebidos").value(1));
    }
}
