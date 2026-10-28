package br.dev.luka.esfinge.cenarios.application;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.cenarios.application.port.out.ScenarioRepository;
import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class ScenarioService implements InspectScenario, br.dev.luka.esfinge.cenarios.application.port.in.AdvanceGeneration {
    private final ScenarioRepository repository;
    private final String scenario, adminKey;
    private final br.dev.luka.esfinge.autenticacao.application.port.in.ListIdentities identities;
    public ScenarioService(ScenarioRepository repository, String scenario, String adminKey, br.dev.luka.esfinge.autenticacao.application.port.in.ListIdentities identities) {
        this.repository = repository; this.scenario = scenario; this.adminKey = adminKey; this.identities = identities;
    }
    public ScenarioSnapshot inspect(String key) {
        if (key == null || !MessageDigest.isEqual(key.getBytes(StandardCharsets.UTF_8), adminKey.getBytes(StandardCharsets.UTF_8)))
            throw new AuthenticationFailure(AuthenticationFailure.Kind.INVALID_CREDENTIALS);
        var state = repository.load(scenario);
        return new ScenarioSnapshot(state.scenario(), state.profile(), state.seed(), state.initialTime(), state.clockMode(),
                state.tokenSeconds(), state.generation(), identities.identities().stream()
                        .map(account -> new ScenarioSnapshot.Identity(account.user(), account.ugs().stream().sorted().toList())).toList());
    }
    public boolean advanceGeneration(String key,long expected) {
        inspect(key);
        return expected>=0 && expected<Long.MAX_VALUE && repository.advanceGeneration(scenario,expected);
    }
}
