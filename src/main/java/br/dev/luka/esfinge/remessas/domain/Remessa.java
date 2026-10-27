package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;

public record Remessa(long id, String key, String scenario, String ug, int yearMonth, Instant openedAt, Instant lastActivity, Instant finalizedAt) {
    public void requireOpen() { if(finalizedAt!=null) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT); }
}
