package br.dev.luka.esfinge.autenticacao.application;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationRepository;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import br.dev.luka.esfinge.autenticacao.domain.Session;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import static br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure.Kind.*;

public final class AuthenticationService implements Authentication, br.dev.luka.esfinge.autenticacao.application.port.in.ListIdentities, br.dev.luka.esfinge.autenticacao.application.port.in.InvalidateSessions {
    private final AuthenticationRepository repository;
    private final Clock clock;
    private final String scenario;
    private final Duration validity;
    private final br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationTransactions transactions;

    public AuthenticationService(AuthenticationRepository repository, Clock clock, String scenario, Duration validity, br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationTransactions transactions) {
        if (scenario == null || scenario.isEmpty() || validity == null || validity.isZero() || validity.isNegative())
            throw new IllegalArgumentException("Configuracao de autenticacao invalida");
        this.repository = repository; this.clock = clock; this.scenario = scenario; this.validity = validity;this.transactions=transactions;
    }

    public java.util.List<br.dev.luka.esfinge.autenticacao.domain.Account> identities() { return repository.identities(scenario); }
    public long invalidateSessions() {return repository.invalidateSessions(scenario);}

    public Session authenticate(String token) {
        if (token == null || token.isEmpty()) throw new AuthenticationFailure(INVALID_CREDENTIALS);
        return repository.findSession(token, scenario).filter(s -> clock.instant().isBefore(s.expiresAt()))
                .orElseThrow(() -> new AuthenticationFailure(INVALID_CREDENTIALS));
    }

    public String login(String accessCode, String password, String ug) {
        if (accessCode == null || accessCode.isEmpty() || password == null || password.isEmpty() || ug == null || ug.isEmpty())
            throw new AuthenticationFailure(INVALID_REQUEST);
        return transactions.execute(()->{
        var account = repository.findAccount(scenario, accessCode, password)
                .orElseThrow(() -> new AuthenticationFailure(INVALID_CREDENTIALS));
        if (!account.ugs().contains(ug)) throw new AuthenticationFailure(FORBIDDEN);
        String token = "sim-" + UUID.randomUUID();
        var now = clock.instant();
        repository.saveSession(token, new Session(account.user(), ug, scenario, now, now.plus(validity)));
        return token;
        });
    }
}
