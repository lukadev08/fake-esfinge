package br.dev.luka.esfinge.configuration;

import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.JdbcAuthenticationRepository;
import javax.sql.DataSource;
import java.time.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
public class Composition {
    @Bean SimulatorSettings settings(Environment environment) { return SimulatorSettings.load(environment); }
    @Bean DataSource dataSource(SimulatorSettings settings) { return SqliteDatabase.open(settings); }
    @Bean Clock businessClock(SimulatorSettings settings) {
        return settings.clockMode.equals("fixed") ? Clock.fixed(settings.initialTime, ZoneOffset.UTC) : Clock.systemUTC();
    }
    @Bean br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario inspection(DataSource source, SimulatorSettings settings, br.dev.luka.esfinge.autenticacao.application.port.in.ListIdentities identities) {
        return new br.dev.luka.esfinge.cenarios.application.ScenarioService(
                new br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository(source), settings.scenario, settings.adminKey, identities);
    }
    @Bean AuthenticationService authentication(DataSource source, Clock clock, SimulatorSettings settings) {
        return new AuthenticationService(new JdbcAuthenticationRepository(source), clock, settings.scenario, settings.tokenValidity);
    }
}
