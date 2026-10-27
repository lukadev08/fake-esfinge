package br.dev.luka.esfinge.remessas.application.port.out;

import br.dev.luka.esfinge.remessas.domain.*;
import java.util.Optional;
import java.util.List;
import java.time.Instant;

public interface RemessaRepository { Remessa open(String scenario, String ug, int yearMonth, String key, Instant now);
    boolean blocked(String scenario,String ug,int yearMonth);
    void block(String scenario,String ug,int yearMonth,boolean blocked);
    Optional<Remessa> find(String key,String scenario,String ug);
    Optional<Remessa> find(String key,String scenario);
    Optional<Remessa> findOpen(String scenario,String ug,int yearMonth);
    List<Remessa> due(String scenario,Instant now);
    Optional<Reception.Plan> receptionPlan(Remessa remessa,Assunto subject);
    void planReception(Remessa remessa,Assunto subject,Reception.Type type,long attempt,Instant now);
    void removeReceptionPlan(Remessa remessa,Assunto subject,Instant now);
    Reception.Type receptionAttempt(Remessa remessa,Assunto subject);
    void responseLost(Remessa remessa,Lote lote,Instant now);
    Lote receive(Remessa remessa,ReceivedContent content,String hash,Instant now);
    Optional<Lote> findLote(long number,String scenario,String ug);
    Optional<Lote> inspectLote(long number,String scenario);
    List<RemessaEvent> events(long remessaId);
    Finalization.State finalization(Remessa remessa);
    void finish(Remessa remessa,Instant now);
    void touch(Lote lote,Instant now);
}
