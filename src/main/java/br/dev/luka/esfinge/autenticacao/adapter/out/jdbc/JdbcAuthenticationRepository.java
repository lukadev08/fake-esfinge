package br.dev.luka.esfinge.autenticacao.adapter.out.jdbc;

import br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationRepository;
import br.dev.luka.esfinge.autenticacao.domain.*;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcAuthenticationRepository implements AuthenticationRepository {
    private final JdbcTemplate jdbc;
    public JdbcAuthenticationRepository(DataSource source) { jdbc = new JdbcTemplate(source); }
    public List<Account> identities(String scenario) {
        return jdbc.queryForList("SELECT user_id FROM users WHERE scenario_id = ? ORDER BY user_id", String.class, scenario)
                .stream().map(user -> new Account(user, new HashSet<>(jdbc.queryForList(
                        "SELECT ug FROM user_ugs WHERE scenario_id = ? AND user_id = ?", String.class, scenario, user)))).toList();
    }
    public Optional<Account> findAccount(String scenario, String code, String password) {
        var accounts = jdbc.query("SELECT user_id, password_hash, salt FROM users WHERE scenario_id = ? AND access_code = ?",
                (rs, row) -> new String[]{rs.getString(1), rs.getString(2), rs.getString(3)}, scenario, code);
        if (accounts.isEmpty()) return Optional.empty();
        var account = accounts.getFirst();
        if (!CredentialHash.matches(CredentialHash.password(password, account[2]), account[1])) return Optional.empty();
        var ugs = jdbc.queryForList("SELECT ug FROM user_ugs WHERE scenario_id = ? AND user_id = ?", String.class, scenario, account[0]);
        return Optional.of(new Account(account[0], new HashSet<>(ugs)));
    }
    public void saveSession(String token, Session session) {
        jdbc.update("INSERT INTO sessions(token_hash, scenario_id, user_id, ug, issued_at, expires_at) VALUES (?, ?, ?, ?, ?, ?)",
                CredentialHash.token(token), session.scenario(), session.user(), session.ug(), session.issuedAt().toString(), session.expiresAt().toString());
    }
    public Optional<Session> findSession(String token, String scenario) {
        return jdbc.query("SELECT user_id, ug, scenario_id, issued_at, expires_at FROM sessions WHERE token_hash = ? AND scenario_id = ?",
                (rs, row) -> new Session(rs.getString(1), rs.getString(2), rs.getString(3), Instant.parse(rs.getString(4)), Instant.parse(rs.getString(5))),
                CredentialHash.token(token), scenario).stream().findFirst();
    }
}
