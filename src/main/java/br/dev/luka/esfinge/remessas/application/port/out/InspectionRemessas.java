package br.dev.luka.esfinge.remessas.application.port.out;

import br.dev.luka.esfinge.remessas.domain.*;
import java.util.*;

public interface InspectionRemessas {
    long ceiling(String scenario,String resource,long id);
    List<Remessa> remessas(Inspection.Filter filter,Inspection.Window window);
    Optional<Remessa> remessa(String scenario,long id);
    List<Inspection.Lot> lotes(Inspection.Filter filter,Inspection.Window window);
    Optional<Inspection.Lot> lote(String scenario,long id);
    List<Inspection.Event> eventos(String scenario,long remessa,Inspection.Window window);
    List<Inspection.Record> recebidos(String scenario,long lot,boolean historical,Inspection.Window window);
    List<Inspection.Counts> counts(String scenario,Long remessa);
}
