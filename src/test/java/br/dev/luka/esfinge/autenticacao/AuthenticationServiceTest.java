package br.dev.luka.esfinge.autenticacao;

import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationRepository;
import br.dev.luka.esfinge.autenticacao.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthenticationServiceTest {
    final Map<String, Session> sessions = new HashMap<>();
    final AuthenticationRepository repository = new AuthenticationRepository() {
        public java.util.List<Account> identities(String scenario) { return java.util.List.of(new Account("usuario-ficticio", Set.of("0001"))); }
        public Optional<Account> findAccount(String scenario, String code, String password) {
            return code.equals("usuario-ficticio") && password.equals("senha-ficticia")
                    ? Optional.of(new Account(code, Set.of("0001"))) : Optional.empty();
        }
        public Optional<Session> findSession(String token, String scenario) { return Optional.ofNullable(sessions.get(token)).filter(s -> s.scenario().equals(scenario)); }
        public void saveSession(String token, Session session) { sessions.put(token, session); }
        public long invalidateSessions(String scenario) {long before=sessions.size();sessions.entrySet().removeIf(entry->entry.getValue().scenario().equals(scenario));return before-sessions.size();}
    };
    final br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationTransactions transactions=new br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationTransactions() {
        public <T> T execute(java.util.function.Supplier<T> operation) {return operation.get();}
    };
    final AuthenticationService service = new AuthenticationService(repository,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC), "cenario-a", Duration.ofHours(1),transactions);

    @Test void autenticaComChaveOpaca() {
        String token = service.login("usuario-ficticio", "senha-ficticia", "0001");
        assertTrue(token.startsWith("sim-"));
        assertEquals(new Session("usuario-ficticio", "0001", "cenario-a", Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T01:00:00Z")), sessions.get(token));
    }
    @Test void tokenMantemContextoAteExpirar() {
        String token = service.login("usuario-ficticio", "senha-ficticia", "0001");
        assertEquals("0001", service.authenticate(token).ug());
    }
    @Test void expiraExatamenteNoLimiteSemEfeitos() {
        String token = service.login("usuario-ficticio", "senha-ficticia", "0001");
        var expired = new AuthenticationService(repository, Clock.fixed(Instant.parse("2026-01-01T01:00:00Z"), ZoneOffset.UTC),
                "cenario-a", Duration.ofHours(1),transactions);
        assertThrows(AuthenticationFailure.class, () -> expired.authenticate(token));
        assertEquals(1, sessions.size());
        assertThrows(AuthenticationFailure.class, () -> service.authenticate("token-inexistente"));
    }
    @Test void outroCenarioNaoAceitaToken() {
        String token = service.login("usuario-ficticio", "senha-ficticia", "0001");
        var other = new AuthenticationService(repository, Clock.systemUTC(), "cenario-b", Duration.ofHours(1),transactions);
        assertThrows(AuthenticationFailure.class, () -> other.authenticate(token));
    }
    @Test void ugNaoAutorizadaNaoEmiteToken() {
        assertEquals(AuthenticationFailure.Kind.FORBIDDEN, assertThrows(AuthenticationFailure.class,
                () -> service.login("usuario-ficticio", "senha-ficticia", "0002")).kind());
        assertTrue(sessions.isEmpty());
    }
    @Test void parametroVazioNaoEmiteToken() {
        assertEquals(AuthenticationFailure.Kind.INVALID_REQUEST, assertThrows(AuthenticationFailure.class,
                () -> service.login("", "senha-ficticia", "0001")).kind());
        assertTrue(sessions.isEmpty());
    }
    @Test void validadeConfiguradaNaoUsaUmaHoraFixa() {
        var custom = new AuthenticationService(repository, Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                "cenario-a", Duration.ofSeconds(30),transactions);
        String token = custom.login("usuario-ficticio", "senha-ficticia", "0001");
        assertEquals(Instant.parse("2026-01-01T00:00:30Z"), sessions.get(token).expiresAt());
    }
    @Test void credencialInvalidaNaoEmiteToken() {
        assertEquals(AuthenticationFailure.Kind.INVALID_CREDENTIALS, assertThrows(AuthenticationFailure.class,
                () -> service.login("invalido", "invalida", "0001")).kind());
        assertTrue(sessions.isEmpty());
    }
}
