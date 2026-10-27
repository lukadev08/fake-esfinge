package br.dev.luka.esfinge.remessas.adapter.out.json;

import br.dev.luka.esfinge.remessas.application.port.out.ProcessRecords;
import br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload;
import br.dev.luka.esfinge.remessas.domain.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;

public final class StructuralRecords implements ProcessRecords {
    private final LotePayload validator=new LotePayload();
    private final ObjectMapper json=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    public List<byte[]> process(Lote lote) {
        var content=validator.read(lote.bytes());
        if(content.subject()!=lote.subject() || content.records()!=lote.records()) throw new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE);
        try {
            var records=json.readTree(content.bytes()).elements().next();var result=new ArrayList<byte[]>();
            for(var record:records) result.add(json.writeValueAsBytes(record));
            return Processing.copy(result);
        } catch(java.io.IOException failure) {throw new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE);}
    }
}
