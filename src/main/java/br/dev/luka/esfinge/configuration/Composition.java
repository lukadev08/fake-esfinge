package br.dev.luka.esfinge.configuration;

import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.JdbcAuthenticationRepository;
import javax.sql.DataSource;
import java.time.*;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
@org.springframework.scheduling.annotation.EnableScheduling
public class Composition {
    @Bean org.springframework.boot.web.server.WebServerFactoryCustomizer<org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory> receptionAbort() {
        return factory->factory.addContextValves(new br.dev.luka.esfinge.remessas.adapter.in.http.ReceptionAbortValve());
    }
    @Bean SimulatorSettings settings(Environment environment) { return SimulatorSettings.load(environment); }
    @Bean DataSource dataSource(SimulatorSettings settings) { return SqliteDatabase.open(settings); }
    @Bean br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcBusinessClock businessClock(SimulatorSettings settings,DataSource source) {
        Clock base=settings.clockMode.equals("fixed") ? Clock.fixed(settings.initialTime, ZoneOffset.UTC) : Clock.systemUTC();
        return new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcBusinessClock(source,settings.scenario,base);
    }
    @Bean br.dev.luka.esfinge.cenarios.application.ScenarioService inspection(DataSource source, SimulatorSettings settings, br.dev.luka.esfinge.autenticacao.application.port.in.ListIdentities identities) {
        return new br.dev.luka.esfinge.cenarios.application.ScenarioService(
                new br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository(source), settings.scenario, settings.adminKey, identities);
    }
    @Bean AuthenticationService authentication(DataSource source, Clock clock, SimulatorSettings settings) {
        return new AuthenticationService(new JdbcAuthenticationRepository(source), clock, settings.scenario, settings.tokenValidity,new br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.JdbcAuthenticationTransactions(source));
    }
    @Bean br.dev.luka.esfinge.remessas.application.port.in.Remessas remessas(Authentication authentication, DataSource source, Clock clock, br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario inspection) {
        return new br.dev.luka.esfinge.remessas.application.RemessaService(authentication,
                new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository(source),
                new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcTransactions(source), clock, inspection,new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcProcessingRepository(source));
    }
    @Bean br.dev.luka.esfinge.remessas.application.port.in.Processamento processamento(Authentication auth,DataSource source,Clock clock,
            br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcBusinessClock time,SimulatorSettings settings,
            br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario inspection,br.dev.luka.esfinge.remessas.application.port.in.Remessas remessas,
            br.dev.luka.esfinge.autenticacao.application.port.in.InvalidateSessions sessions,br.dev.luka.esfinge.cenarios.application.port.in.AdvanceGeneration generations) {
        return new br.dev.luka.esfinge.remessas.application.ProcessingService(auth,inspection,remessas,
            new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository(source),
            new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcProcessingRepository(source),
            new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcTransactions(source),clock,time,
            new br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords(),settings.scenario,sessions,generations);
    }
    @Bean br.dev.luka.esfinge.remessas.adapter.in.scheduler.ProcessingScheduler processingScheduler(br.dev.luka.esfinge.remessas.application.port.in.Processamento processing) {
        return new br.dev.luka.esfinge.remessas.adapter.in.scheduler.ProcessingScheduler(processing);
    }
    @Bean br.dev.luka.esfinge.remessas.application.port.in.InspectExecution executionInspection(DataSource source, Clock clock, br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario scenario) {
        var remessas=new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository(source);
        var processing=new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcProcessingRepository(source);
        return new br.dev.luka.esfinge.remessas.application.InspectionService(scenario,remessas,processing,processing,new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcTransactions(source),clock);
    }
}
