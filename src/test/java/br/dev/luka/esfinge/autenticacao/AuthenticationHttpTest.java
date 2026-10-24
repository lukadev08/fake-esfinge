package br.dev.luka.esfinge.autenticacao;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.*;
import org.springframework.boot.test.context.TestConfiguration;
import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {SimulatorApplication.class, AuthenticationHttpTest.TimeConfiguration.class})
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
class AuthenticationHttpTest {
    static final AtomicReference<Instant> NOW = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock testClock() { return new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { return NOW.get(); }
        }; }
    }
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @BeforeEach void time() { NOW.set(Instant.parse("2026-01-01T00:00:00Z")); }
    private MockHttpServletRequestBuilder login() {
        return post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a").header("senha", "senha-ficticia-a")
                .param("codigoUg", "0001").param("descricaoEmpresaTI", "Empresa ficticia").param("descritivoSoftware", "Software ficticio");
    }
    private String token() throws Exception {
        return json.readTree(http.perform(login()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database", () -> {
            try { return Files.createTempDirectory("esfinge-http-").resolve("test.sqlite").toString(); }
            catch (Exception e) { throw new IllegalStateException(); }
        });
    }
    @Test void credenciaisInvalidasRetornam401() throws Exception {
        http.perform(post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a").header("senha", "senha-errada")
                .param("codigoUg", "0001").param("descricaoEmpresaTI", "Empresa").param("descritivoSoftware", "Software"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("SIM_AUTENTICACAO_INVALIDA"));
    }
    @Test void ugNaoAutorizadaRetorna403() throws Exception {
        http.perform(post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a").header("senha", "senha-ficticia-a")
                .param("codigoUg", "0002").param("descricaoEmpresaTI", "Empresa").param("descritivoSoftware", "Software"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("SIM_ACESSO_NEGADO"));
    }
    @Test void corpoParametrosAusentesVaziosExtrasEDuplicadosRetornam400() throws Exception {
        http.perform(login().content("{}")).andExpect(status().isBadRequest());
        http.perform(post("/autenticacao/login")).andExpect(status().isBadRequest());
        http.perform(login().param("extra", "x")).andExpect(status().isBadRequest());
        http.perform(login().param("codigoUg", "0001")).andExpect(status().isBadRequest());
        http.perform(login().header("senha", "outra")).andExpect(status().isBadRequest());
        http.perform(post("/autenticacao/login").header("codigoAcesso", "").header("senha", "senha-ficticia-a")
                .param("codigoUg", "0001").param("descricaoEmpresaTI", "Empresa").param("descritivoSoftware", "Software"))
                .andExpect(status().isBadRequest());
    }
    @Test void tokenExpiraSemEfeitosELoginPermiteRenovacao() throws Exception {
        String token = token();
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", token)).andExpect(status().isUnprocessableEntity());
        NOW.set(Instant.parse("2026-01-01T01:00:00Z"));
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("SIM_AUTENTICACAO_INVALIDA"));
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", token())).andExpect(status().isUnprocessableEntity());
        NOW.set(Instant.parse("2026-01-01T00:00:00Z"));
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", token)).andExpect(status().isUnprocessableEntity());
    }
    @Test void chavesAdministrativaECompativelNaoSeIntercambiam() throws Exception {
        String token = token();
        http.perform(get("/__simulator/configuracao")).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/configuracao").header("X-Simulator-Key", token)).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", "admin-local-ficticio")).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/iniciarEnvio")).andExpect(status().isUnauthorized());
        http.perform(get("/recurso-inexistente")).andExpect(status().isNotFound());
    }
    @Test void administracaoEErrosNaoExpoemCredenciaisOuDetalhesTecnicos() throws Exception {
        String body = http.perform(get("/__simulator/configuracao").header("X-Simulator-Key", "admin-local-ficticio"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("senha-ficticia-a") || body.contains("admin-local-ficticio") || body.contains("acesso-ficticio-a"));
        http.perform(post("/autenticacao/login")).andExpect(jsonPath("$.correlacao").isString())
                .andExpect(jsonPath("$.trace").doesNotExist()).andExpect(jsonPath("$.exception").doesNotExist());
    }
    @Test void inspecaoAdministrativaUsaChavePropria() throws Exception {
        http.perform(get("/__simulator/configuracao").header("X-Simulator-Key", "admin-local-ficticio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.perfil").value("2026/V6/6.06"))
                .andExpect(jsonPath("$.cenario").value("cenario-ficticio-a"));
    }
    @Test void loginHttpRetornaSomenteChave() throws Exception {
        http.perform(post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a").header("senha", "senha-ficticia-a")
                .param("codigoUg", "0001").param("descricaoEmpresaTI", "Empresa ficticia").param("descritivoSoftware", "Software ficticio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.chave").isString()).andExpect(jsonPath("$.auth_token").doesNotExist());
    }
}
