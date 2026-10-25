package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.time.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes={SimulatorApplication.class,RemessaExpiryHttpTest.TimeConfiguration.class})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
class RemessaExpiryHttpTest {
    static final AtomicReference<Instant> NOW=new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
    @TestConfiguration static class TimeConfiguration {
        @Bean @Primary Clock testClock() { return new Clock(){
            public ZoneId getZone(){return ZoneOffset.UTC;} public Clock withZone(ZoneId zone){return this;} public Instant instant(){return NOW.get();}
        }; }
    }
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-expiry-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
    }
    String token() throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
                .param("codigoUg","0001").param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    @Test void expiraAntesDeAberturaRecepcaoEConsultaSemEfeitos() throws Exception {
        NOW.set(Instant.parse("2026-01-01T00:00:00Z"));String expired=token();NOW.set(NOW.get().plusSeconds(3600));
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",expired).param("anoMes","202601").param("cancelamentoAutomatico","NAO")).andExpect(status().isUnauthorized());
        String current=token();String key=json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",current).param("anoMes","202601")
                .param("cancelamentoAutomatico","NAO")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
        NOW.set(NOW.get().plusSeconds(3600));
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",current).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isUnauthorized());
        current=token();long number=json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",current).param("chavePacote",key)
                .contentType("application/json").content(RemessaHttpTest.PAYLOAD)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        NOW.set(NOW.get().plusSeconds(3600));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",current)).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",token())).andExpect(status().isOk());
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.recebidos").value(1)).andExpect(jsonPath("$.eventos.length()").value(3))
                .andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0));
    }
}
