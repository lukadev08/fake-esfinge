package br.dev.luka.esfinge.autenticacao.domain;

import java.time.Instant;

public record Session(String user, String ug, String scenario, Instant issuedAt, Instant expiresAt) {}
