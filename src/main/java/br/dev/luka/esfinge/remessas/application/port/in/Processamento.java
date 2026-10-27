package br.dev.luka.esfinge.remessas.application.port.in;

import br.dev.luka.esfinge.remessas.domain.Processing;

public interface Processamento {
    Processing.Started start(String token,String key);
    Processing.Consultation consult(String token,long number);
    Processing.Snapshot inspect(String adminKey,long number);
    Processing.Configuration configuration(String adminKey);
    Processing.Configuration configure(String adminKey,Processing.Mode mode,long latencyMillis);
    Processing.Advanced advance(String adminKey,long millis,int steps);
    Processing.FailurePlan failurePlan(String adminKey,long number);
    Processing.FailurePlan planFailure(String adminKey,long number,Integer record);
    void removeFailure(String adminKey,long number);
    void tick();
}
