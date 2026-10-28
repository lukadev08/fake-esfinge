package br.dev.luka.esfinge.remessas.application.port.in;

import br.dev.luka.esfinge.remessas.domain.*;
import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;
import java.util.List;

public interface InspectExecution {
    ScenarioSnapshot authorize(String key);
    Inspection.Page<Remessa> remessas(String key,Inspection.Filter filter,int limit,String cursor);
    Inspection.Page<Inspection.LotView> lotes(String key,Inspection.Filter filter,int limit,String cursor);
    RemessaDetail remessa(String key,long id);
    Inspection.Page<Inspection.Event> eventos(String key,long id,int limit,String cursor,String binding);
    Inspection.Page<Inspection.Record> records(String key,long id,Inspection.Type type,int limit,String cursor);
    Report report(String key,int limit,String cursor,String origin);
    record RemessaDetail(Remessa remessa,List<Inspection.Counts> recebimento,List<Inspection.ResultCounts> processamento) {}
    record Report(ScenarioSnapshot configuracao,Processing.Configuration processamento,List<Inspection.Counts> recebimento,List<Inspection.ResultCounts> resultados,Inspection.Page<Inspection.Event> sequencia) {}
}
