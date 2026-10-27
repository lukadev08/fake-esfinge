package br.dev.luka.esfinge.remessas.application.port.out;

import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Instant;
import java.util.*;

public interface ProcessingRepository {
    Processing.Configuration configuration(String scenario,Instant now);
    void configure(String scenario,Processing.Mode mode,long latencyMillis);
    long generation(String scenario);
    boolean active(long remessaId,long generation);
    List<Long> eligible(Remessa remessa);
    void schedule(Remessa remessa,List<Long> lots,long generation,Instant now,long latencyMillis);
    Optional<Processing.Work> next(String scenario,Instant now);
    boolean begin(Processing.Work work,Instant due);
    boolean complete(Processing.Work work,List<byte[]> records,Instant now);
    Optional<Processing.FailurePlan> failurePlan(long lot,String scenario,long generation);
    void planFailure(Lote lot,long generation,Integer record,Instant now);
    void removeFailure(Lote lot,long generation,Instant now);
    boolean fail(Processing.Work work,Instant now);
    void discard(Remessa remessa,Assunto subject,long generation,Instant now);
    void cancel(Remessa remessa,long generation,Instant now);
    boolean isActive(long lot);
    Processing.Diagnostic diagnostic(long lot);
    Processing.State state(long lot);
    List<byte[]> processed(long lot);
    List<byte[]> effective(long lot);
}
