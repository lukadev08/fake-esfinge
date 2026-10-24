package br.dev.luka.esfinge.autenticacao.application.port.out;

import br.dev.luka.esfinge.autenticacao.domain.Account;
import br.dev.luka.esfinge.autenticacao.domain.Session;
import java.util.Optional;

public interface AuthenticationRepository {
    java.util.List<Account> identities(String scenario);
    Optional<Account> findAccount(String scenario, String accessCode, String password);
    Optional<Session> findSession(String token, String scenario);
    void saveSession(String token, Session session);
}
