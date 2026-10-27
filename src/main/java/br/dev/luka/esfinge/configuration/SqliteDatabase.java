package br.dev.luka.esfinge.configuration;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.CredentialHash;
import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.JdbcAuthenticationRepository;
import javax.sql.DataSource;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import org.sqlite.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.support.TransactionTemplate;

public final class SqliteDatabase {
    private SqliteDatabase() {}
    public static DataSource open(SimulatorSettings settings) {
        try {
            Path path = Path.of(settings.database).toAbsolutePath();
            Files.createDirectories(path.getParent());
            var config = new SQLiteConfig(); config.enforceForeignKeys(true); config.setBusyTimeout(5000); config.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
            var source = new SQLiteDataSource(config); source.setUrl("jdbc:sqlite:" + path);
            migrate(source);
            seed(source, settings);
            return source;
        } catch (Exception e) { throw new IllegalStateException("Base SQLite indisponivel ou incompativel; dados preservados"); }
    }
    private static void migrate(DataSource source) throws Exception {
        String[] scripts = {"V1__authentication.sql", "V2__remessas.sql", "V3__creditos.sql", "V4__arrecadacao.sql", "V5__baixas.sql", "V6__saldo.sql", "V7__prazo.sql", "V8__processamento.sql", "V9__finalizacao.sql", "V10__erro_processamento.sql", "V11__cancelamento.sql", "V12__falhas_recepcao.sql", "V13__bloqueio_competencia.sql", "V14__expiracao.sql"};
        try (var connection = source.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int version;
                try (var statement = connection.createStatement(); var result = statement.executeQuery("PRAGMA user_version")) { version = result.getInt(1); }
                if (version < 0 || version > scripts.length) throw new IllegalStateException();
                if (version == 0) {
                    try (var statement = connection.createStatement(); var result = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")) {
                        if (result.getInt(1) != 0) throw new IllegalStateException();
                    }
                } else {
                    try (var statement = connection.createStatement(); var result = statement.executeQuery("SELECT version, checksum FROM schema_migrations ORDER BY version")) {
                        for (int i=0;i<version;i++) {
                            String checksum=CredentialHash.token(new ClassPathResource("db/migration/"+scripts[i]).getContentAsString(StandardCharsets.UTF_8));
                            if (!result.next() || result.getInt(1)!=i+1 || !checksum.equals(result.getString(2))) throw new IllegalStateException();
                        }
                        if (result.next()) throw new IllegalStateException();
                    }
                }
                for (int i=version;i<scripts.length;i++) {
                    var resource=new ClassPathResource("db/migration/"+scripts[i]);
                    ScriptUtils.executeSqlScript(connection,resource);
                    if(i+1==14) backfillExpiration(connection);
                    try (var statement=connection.prepareStatement("INSERT INTO schema_migrations(version,checksum) VALUES (?,?)")) {
                        statement.setInt(1,i+1);statement.setString(2,CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));statement.executeUpdate();
                    }
                }
                connection.commit();
            } catch (Exception e) { connection.rollback(); throw e; }
        }
    }
    private static void backfillExpiration(java.sql.Connection connection) throws java.sql.SQLException {
        try(var query=connection.createStatement();var rows=query.executeQuery("SELECT id,atividade FROM remessas");
            var update=connection.prepareStatement("UPDATE remessas SET expira_segundos=?,expira_nanos=? WHERE id=?")) {
            while(rows.next()) {
                var due=br.dev.luka.esfinge.remessas.domain.Remessa.deadline(java.time.Instant.parse(rows.getString("atividade")));
                update.setLong(1,due.getEpochSecond());update.setInt(2,due.getNano());update.setLong(3,rows.getLong("id"));update.executeUpdate();
            }
        }
    }
    private static void seed(DataSource source, SimulatorSettings settings) {
        var jdbc = new JdbcTemplate(source);
        new TransactionTemplate(new DataSourceTransactionManager(source)).executeWithoutResult(status -> {
            var existing = jdbc.queryForList("SELECT profile, seed, initial_time, clock_mode, token_seconds FROM scenarios WHERE id = ?", settings.scenario);
            if (!existing.isEmpty()) {
                var row = existing.getFirst();
                if (!settings.profile.equals(row.get("profile")) || settings.seed != ((Number) row.get("seed")).longValue()
                        || !settings.initialTime.toString().equals(row.get("initial_time")) || !settings.clockMode.equals(row.get("clock_mode"))
                        || settings.tokenValidity.toSeconds() != ((Number) row.get("token_seconds")).longValue()
                        || jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE scenario_id = ?", Integer.class, settings.scenario) != settings.users.size())
                    throw new IllegalStateException();
                var auth = new JdbcAuthenticationRepository(source);
                for (var user : settings.users) {
                    var stored = auth.findAccount(settings.scenario, user.accessCode(), user.password()).orElseThrow(IllegalStateException::new);
                    if (!stored.user().equals(user.id()) || !stored.ugs().equals(user.ugs())) throw new IllegalStateException();
                }
                if (jdbc.queryForObject("SELECT COUNT(*) FROM processamento_config WHERE cenario=?",Integer.class,settings.scenario)!=1
                        || jdbc.queryForObject("SELECT COUNT(*) FROM relogio_negocio WHERE cenario=?",Integer.class,settings.scenario)!=1) throw new IllegalStateException();
                return;
            }
            jdbc.update("INSERT INTO scenarios(id, profile, seed, initial_time, clock_mode, token_seconds) VALUES (?, ?, ?, ?, ?, ?)",
                    settings.scenario, settings.profile, settings.seed, settings.initialTime.toString(), settings.clockMode, settings.tokenValidity.toSeconds());
            for (var user : settings.users) {
                String salt = CredentialHash.salt();
                jdbc.update("INSERT INTO users(scenario_id, user_id, access_code, password_hash, salt) VALUES (?, ?, ?, ?, ?)",
                        settings.scenario, user.id(), user.accessCode(), CredentialHash.password(user.password(), salt), salt);
                for (String ug : user.ugs()) jdbc.update("INSERT INTO user_ugs(scenario_id, user_id, ug) VALUES (?, ?, ?)", settings.scenario, user.id(), ug);
            }
            jdbc.update("INSERT INTO processamento_config(cenario) VALUES (?)",settings.scenario);
            jdbc.update("INSERT INTO relogio_negocio(cenario) VALUES (?)",settings.scenario);
        });
    }
}
