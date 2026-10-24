package br.dev.luka.esfinge.cenarios.domain;

import java.time.Instant;
import java.util.List;

public record ScenarioSnapshot(String scenario, String profile, long seed, Instant initialTime, String clockMode,
        long tokenSeconds, long generation, List<Identity> identities) {
    public ScenarioSnapshot { identities = List.copyOf(identities); }
    public record Identity(String user, List<String> ugs) {
        public Identity { ugs = List.copyOf(ugs); }
    }
}
