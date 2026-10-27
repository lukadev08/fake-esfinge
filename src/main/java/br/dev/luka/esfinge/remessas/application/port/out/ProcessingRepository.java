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
    Processing.State state(long lot);
    List<byte[]> processed(long lot);
    List<byte[]> effective(long lot);
}
