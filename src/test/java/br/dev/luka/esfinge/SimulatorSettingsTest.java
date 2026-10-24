package br.dev.luka.esfinge;

import br.dev.luka.esfinge.configuration.SimulatorSettings;
import org.springframework.mock.env.MockEnvironment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulatorSettingsTest {
    MockEnvironment valid() {
        return new MockEnvironment().withProperty("simulator.database", "/tmp/test.sqlite")
                .withProperty("simulator.scenario", "outro-cenario").withProperty("simulator.profile", "2026/V6/6.06")
                .withProperty("simulator.admin-key", "admin-ficticio").withProperty("simulator.clock-mode", "fixed")
                .withProperty("simulator.seed", "42").withProperty("simulator.initial-time", "2026-02-01T00:00:00Z")
                .withProperty("simulator.token-seconds", "30").withProperty("simulator.users[0].id", "usuario-b")
                .withProperty("simulator.users[0].access-code", "acesso-b").withProperty("simulator.users[0].password", "senha-b")
                .withProperty("simulator.users[0].ugs", "0002,0003");
    }
    @Test void contextoEIdentidadesSaoConfiguraveis() {
        var settings = SimulatorSettings.load(valid());
        assertEquals("outro-cenario", settings.scenario);
        assertEquals(30, settings.tokenValidity.toSeconds());
        assertEquals(java.util.Set.of("0002", "0003"), settings.users.getFirst().ugs());
        assertEquals(42, settings.seed);
    }
    @Test void erroDeConfiguracaoNaoExpoeValorOuCausa() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> SimulatorSettings.load(valid().withProperty("simulator.token-seconds", "segredo-ficticio-invalido")));
        assertEquals("Configuracao do simulador invalida", error.getMessage());
        assertNull(error.getCause());
    }
    @Test void perfilDiferenteEValidadeNaoPositivaSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> SimulatorSettings.load(valid().withProperty("simulator.profile", "2027/V7/7.00")));
        assertThrows(IllegalArgumentException.class, () -> SimulatorSettings.load(valid().withProperty("simulator.token-seconds", "0")));
    }
    @Test void chaveAdministrativaNaoReutilizaCredenciais() {
        assertThrows(IllegalArgumentException.class, () -> SimulatorSettings.load(valid().withProperty("simulator.admin-key", "senha-b")));
        assertFalse(SimulatorSettings.load(valid()).users.getFirst().toString().contains("senha-b"));
    }
}
