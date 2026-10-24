package br.dev.luka.esfinge.autenticacao;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.JdbcAuthenticationRepository;
import br.dev.luka.esfinge.autenticacao.domain.Session;
import br.dev.luka.esfinge.configuration.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class JdbcAuthenticationRepositoryTest {
    @TempDir Path temporary;
    SimulatorSettings settings(String scenario) {
        return new SimulatorSettings(temporary.resolve("state.sqlite").toString(), scenario, "2026/V6/6.06", "admin-ficticio", "fixed", 1,
                Instant.parse("2026-01-01T00:00:00Z"), Duration.ofHours(1),
                List.of(new SimulatorSettings.User("usuario-a", "acesso-ficticio", "senha-ficticia", Set.of("0001"))));
    }
    Session session(String ug) {
        return new Session("usuario-a", ug, "cenario-a", Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T01:00:00Z"));
    }
    @Test void reaberturaConservaSessaoEContexto() throws Exception {
        var settings = settings("cenario-a"); var source = SqliteDatabase.open(settings);
        var repository = new JdbcAuthenticationRepository(source);
        repository.saveSession("sim-token-ficticio", session("0001"));
        assertEquals(session("0001"), new JdbcAuthenticationRepository(SqliteDatabase.open(settings)).findSession("sim-token-ficticio", "cenario-a").orElseThrow());
        assertTrue(repository.findSession("sim-token-ficticio", "cenario-b").isEmpty());
        String bytes = new String(Files.readAllBytes(Path.of(settings.database)), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertFalse(bytes.contains("sim-token-ficticio") || bytes.contains("senha-ficticia"));
    }
    @Test void credenciaisEVinculosReabremSemCoercao() {
        var repository = new JdbcAuthenticationRepository(SqliteDatabase.open(settings("cenario-a")));
        assertEquals(Set.of("0001"), repository.findAccount("cenario-a", "acesso-ficticio", "senha-ficticia").orElseThrow().ugs());
        assertTrue(repository.findAccount("cenario-a", "acesso-ficticio", "errada").isEmpty());
        assertTrue(repository.findAccount("cenario-b", "acesso-ficticio", "senha-ficticia").isEmpty());
        assertThrows(org.springframework.dao.DataAccessException.class, () -> repository.saveSession("invalido", session("1")));
        assertTrue(repository.findSession("invalido", "cenario-a").isEmpty());
    }
    @Test void chavesEstrangeirasAtivasEmCadaConexao() throws Exception {
        var source = SqliteDatabase.open(settings("cenario-a"));
        for (int i = 0; i < 2; i++) try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("PRAGMA foreign_keys")) { assertEquals(1, result.getInt(1)); }
            assertThrows(java.sql.SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO user_ugs(scenario_id,user_id,ug) VALUES ('inexistente','inexistente','0001')"));
        }
    }
    @Test void versaoIncompativelNaoRecriaDados() throws Exception {
        var source = SqliteDatabase.open(settings("cenario-a"));
        new JdbcAuthenticationRepository(source).saveSession("preservado", session("0001"));
        try (var connection = source.getConnection(); var statement = connection.createStatement()) { statement.execute("PRAGMA user_version=99"); }
        var error = assertThrows(IllegalStateException.class, () -> SqliteDatabase.open(settings("cenario-a")));
        assertNull(error.getCause());
        assertEquals(session("0001"), new JdbcAuthenticationRepository(source).findSession("preservado", "cenario-a").orElseThrow());
    }
    @Test void bancoDesconhecidoNaoEInicializadoSilenciosamente() throws Exception {
        var source = new SQLiteDataSource(); source.setUrl("jdbc:sqlite:" + settings("cenario-a").database);
        try (var connection = source.getConnection(); var statement = connection.createStatement()) { statement.execute("CREATE TABLE dado_original(valor TEXT)"); statement.execute("INSERT INTO dado_original VALUES ('preservado')"); }
        assertThrows(IllegalStateException.class, () -> SqliteDatabase.open(settings("cenario-a")));
        try (var connection = source.getConnection(); var statement = connection.createStatement(); var result = statement.executeQuery("SELECT valor FROM dado_original")) {
            assertEquals("preservado", result.getString(1));
        }
    }
    @Test void configuracaoIncompativelNaoSobrescreveCenario() {
        var original = settings("cenario-a"); var source = SqliteDatabase.open(original);
        new JdbcAuthenticationRepository(source).saveSession("preservado", session("0001"));
        var changed = new SimulatorSettings(original.database, original.scenario, original.profile, original.adminKey, original.clockMode,
                original.seed, original.initialTime, Duration.ofMinutes(30), original.users);
        assertThrows(IllegalStateException.class, () -> SqliteDatabase.open(changed));
        assertEquals(session("0001"), new JdbcAuthenticationRepository(source).findSession("preservado", "cenario-a").orElseThrow());
    }
}
