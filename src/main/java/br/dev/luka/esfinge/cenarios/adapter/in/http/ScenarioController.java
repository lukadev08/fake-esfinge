package br.dev.luka.esfinge.cenarios.adapter.in.http;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public final class ScenarioController {
    private final InspectScenario inspection;
    public ScenarioController(InspectScenario inspection) { this.inspection = inspection; }
    public record IdentityResponse(String usuario, List<String> ugs) {}
    public record ConfigurationResponse(String cenario, String perfil, long seed, Instant instanteInicial, String modoRelogio,
            long validadeTokenSegundos, long geracao, List<IdentityResponse> identidades) {}
    @GetMapping("/__simulator/configuracao")
    public ConfigurationResponse inspect(@RequestHeader(value = "X-Simulator-Key", required = false) String key) {
        ScenarioSnapshot state = inspection.inspect(key);
        return new ConfigurationResponse(state.scenario(), state.profile(), state.seed(), state.initialTime(), state.clockMode(),
                state.tokenSeconds(), state.generation(), state.identities().stream().map(i -> new IdentityResponse(i.user(), i.ugs())).toList());
    }
}
