package br.dev.luka.esfinge.cenarios.adapter.out.jdbc;

import br.dev.luka.esfinge.cenarios.application.port.out.ScenarioRepository;
import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;
import javax.sql.DataSource;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcScenarioRepository implements ScenarioRepository {
    private final JdbcTemplate jdbc;
    public JdbcScenarioRepository(DataSource source) { jdbc = new JdbcTemplate(source); }
    public ScenarioSnapshot load(String scenario) {
        return jdbc.queryForObject("SELECT id, profile, seed, initial_time, clock_mode, token_seconds, generation FROM scenarios WHERE id = ?",
                (rs, row) -> new ScenarioSnapshot(rs.getString(1), rs.getString(2), rs.getLong(3), Instant.parse(rs.getString(4)),
                        rs.getString(5), rs.getLong(6), rs.getLong(7), java.util.List.of()), scenario);
    }
    public boolean advanceGeneration(String scenario,long expected) {
        return jdbc.update("UPDATE scenarios SET generation=generation+1 WHERE id=? AND generation=? AND generation<9223372036854775807",scenario,expected)==1;
    }
}
