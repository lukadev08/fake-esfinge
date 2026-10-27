package br.dev.luka.esfinge.remessas.application.port.in;

import br.dev.luka.esfinge.remessas.domain.*;

public interface Remessas { Remessa open(String token, int yearMonth, String cancellation); Lote receive(String token,String key,ReceivedContent content);
    void finish(String token,String key,Finalization totals);
    Lote consult(String token,long number);
    LoteInspection inspect(String adminKey,long number);
    Reception.Plan receptionPlan(String adminKey,String key,Assunto subject);
    Reception.Plan planReception(String adminKey,String key,Assunto subject,Reception.Type type,long attempt);
    void removeReceptionPlan(String adminKey,String key,Assunto subject);
    Competencia competencia(String adminKey,String ug,int yearMonth);
    Competencia blockCompetencia(String adminKey,String ug,int yearMonth,boolean blocked);
}
