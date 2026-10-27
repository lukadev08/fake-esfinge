package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;

public record Remessa(long id, String key, String scenario, String ug, int yearMonth, Instant openedAt, Instant lastActivity, Instant finalizedAt, Instant cancelledAt, Instant expiresAt, Instant expiredAt) {
    public static Instant deadline(Instant activity) {return activity.plusSeconds(72*60*60);}
    public boolean isOpen() {return finalizedAt==null && cancelledAt==null && expiredAt==null;}
    public boolean due(Instant now) {return isOpen() && !now.isBefore(expiresAt);}
    public void requireOpen() {if(!isOpen()) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);}
    public void requireOpen(Instant now) {requireOpen();if(due(now)) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);}
}
