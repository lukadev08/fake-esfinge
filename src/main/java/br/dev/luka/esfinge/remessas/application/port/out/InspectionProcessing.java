package br.dev.luka.esfinge.remessas.application.port.out;

import br.dev.luka.esfinge.remessas.domain.*;
import java.util.List;

public interface InspectionProcessing {
    Inspection.State summary(String scenario,long lot);
    List<Inspection.Record> records(String scenario,long lot,Inspection.Type type,Inspection.Window window);
    List<Inspection.ResultCounts> counts(String scenario,Long remessa);
}
