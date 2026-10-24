package br.dev.luka.esfinge.autenticacao.domain;

import java.util.Set;

public record Account(String user, Set<String> ugs) {
    public Account { ugs = Set.copyOf(ugs); }
}
