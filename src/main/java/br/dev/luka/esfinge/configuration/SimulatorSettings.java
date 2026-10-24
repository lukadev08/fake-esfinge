package br.dev.luka.esfinge.configuration;

import java.time.*;
import java.util.*;
import org.springframework.core.env.Environment;

public final class SimulatorSettings {
    public record User(String id, String accessCode, String password, Set<String> ugs) {
        public User {
            ugs = Set.copyOf(ugs);
            if (id == null || id.isEmpty() || accessCode == null || accessCode.isEmpty() || password == null || password.isEmpty()
                    || ugs.isEmpty() || ugs.stream().anyMatch(String::isEmpty)) throw invalid();
        }
        @Override public String toString() { return "Usuario ficticio configurado"; }
    }
    public final String database, scenario, profile, adminKey, clockMode;
    public final long seed;
    public final Instant initialTime;
    public final Duration tokenValidity;
    public final List<User> users;

    public SimulatorSettings(String database, String scenario, String profile, String adminKey, String clockMode,
            long seed, Instant initialTime, Duration tokenValidity, List<User> users) {
        if (database == null || database.isEmpty() || scenario == null || scenario.isEmpty() || !"2026/V6/6.06".equals(profile)
                || adminKey == null || adminKey.isEmpty() || !Set.of("system", "fixed").contains(clockMode)
                || initialTime == null || tokenValidity == null || tokenValidity.isZero() || tokenValidity.isNegative() || users.isEmpty()
                || users.stream().anyMatch(u -> adminKey.equals(u.password()) || adminKey.equals(u.accessCode()))
                || users.stream().map(User::id).distinct().count() != users.size()
                || users.stream().map(User::accessCode).distinct().count() != users.size()) throw invalid();
        this.database = database; this.scenario = scenario; this.profile = profile; this.adminKey = adminKey;
        this.clockMode = clockMode; this.seed = seed; this.initialTime = initialTime; this.tokenValidity = tokenValidity; this.users = List.copyOf(users);
    }
    public static SimulatorSettings load(Environment env) {
        try {
            var users = new ArrayList<User>();
            for (int i = 0; env.containsProperty("simulator.users[" + i + "].id"); i++) {
                String prefix = "simulator.users[" + i + "].";
                users.add(new User(env.getRequiredProperty(prefix + "id"), env.getRequiredProperty(prefix + "access-code"),
                        env.getRequiredProperty(prefix + "password"), new HashSet<>(List.of(env.getRequiredProperty(prefix + "ugs").split(",", -1)))));
            }
            return new SimulatorSettings(env.getRequiredProperty("simulator.database"), env.getRequiredProperty("simulator.scenario"),
                    env.getRequiredProperty("simulator.profile"), env.getRequiredProperty("simulator.admin-key"),
                    env.getRequiredProperty("simulator.clock-mode"), Long.parseLong(env.getRequiredProperty("simulator.seed")),
                    Instant.parse(env.getRequiredProperty("simulator.initial-time")),
                    Duration.ofSeconds(Long.parseLong(env.getRequiredProperty("simulator.token-seconds"))), users);
        } catch (RuntimeException e) { throw invalid(); }
    }
    private static IllegalArgumentException invalid() { return new IllegalArgumentException("Configuracao do simulador invalida"); }
}
