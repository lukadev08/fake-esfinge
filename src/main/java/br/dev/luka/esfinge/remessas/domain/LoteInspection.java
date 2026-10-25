package br.dev.luka.esfinge.remessas.domain;

import java.util.List;
public record LoteInspection(Lote lote,List<RemessaEvent> events) {
    public LoteInspection { events=List.copyOf(events); }
}
