package br.dev.luka.esfinge.remessas.application.port.out;

import java.time.Instant;

public interface BusinessTime {
    void advance(long millis);
    /** Rejeita instantes fora do formato contratual ou do deslocamento persistivel. */
    void validate(Instant instant);
}
