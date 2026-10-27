package br.dev.luka.esfinge.remessas.domain;

import java.math.BigInteger;
import java.util.*;

public record Finalization(Map<Assunto,BigInteger> totals) {
    public Finalization {
        if(totals==null || !totals.keySet().equals(EnumSet.allOf(Assunto.class))
                || totals.values().stream().anyMatch(value->value==null || value.signum()<0))
            throw new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE);
        totals=Map.copyOf(totals);
    }
    public record State(long lots,long successful,Map<Assunto,Long> effective) {}
    public void verify(State state) {
        if(state.lots()<1 || state.lots()!=state.successful()) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        for(var subject:Assunto.values()) if(!totals.get(subject).equals(BigInteger.valueOf(state.effective().getOrDefault(subject,0L))))
            throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
    }
}
