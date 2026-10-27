package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;

public record Remessa(long id, String key, String scenario, String ug, int yearMonth, Instant openedAt, Instant lastActivity, Instant finalizedAt, Instant cancelledAt) {
    public void requireOpen() { if(finalizedAt!=null || cancelledAt!=null) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT); }
}
