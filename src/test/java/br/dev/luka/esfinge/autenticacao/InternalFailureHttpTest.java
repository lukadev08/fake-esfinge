package br.dev.luka.esfinge.autenticacao;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import javax.sql.DataSource;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.system.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimulatorApplication.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@DirtiesContext
@ExtendWith(OutputCaptureExtension.class)
class InternalFailureHttpTest {
    @Autowired MockMvc http;
    @Autowired DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database", () -> {
            try { return Files.createTempDirectory("esfinge-failure-").resolve("test.sqlite").toString(); }
            catch (Exception e) { throw new IllegalStateException(); }
        });
    }
    @Test void falhaSQLiteNaoConfirmaLoginNemExpoeSqlOuCredenciais(CapturedOutput output) throws Exception {
        try (var connection = source.getConnection(); var statement = connection.createStatement()) { statement.execute("DROP TABLE sessions"); }
        String error = http.perform(post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a").header("senha", "senha-ficticia-a")
                .param("codigoUg", "0001").param("descricaoEmpresaTI", "Empresa").param("descritivoSoftware", "Software"))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("SIM_ERRO_INTERNO"))
                .andExpect(jsonPath("$.chave").doesNotExist()).andReturn().getResponse().getContentAsString();
        String observed = error + output.getAll();
        assertFalse(observed.contains("INSERT INTO") || observed.contains("no such table") || observed.contains("senha-ficticia-a")
                || observed.contains("acesso-ficticio-a") || observed.contains("JdbcAuthenticationRepository"));
    }
}
