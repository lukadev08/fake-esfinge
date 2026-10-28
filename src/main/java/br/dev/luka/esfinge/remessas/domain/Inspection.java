package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;
import java.util.List;

public final class Inspection {
    private Inspection() {}
    public enum Type { RECEBIDOS, PROCESSADOS, EFETIVOS, HISTORICOS }
    public record Filter(String cenario,String codigoUg,Integer anoMes,Assunto assunto) {}
    public record Window(long depois,long teto,int limite) {}
    public record Page<T>(List<T> itens,String proximoCursor,long geracao,long teto) {public Page {itens=List.copyOf(itens);}}
    public record Lot(long numeroLote,long identificadorRemessa,String chavePacote,String cenario,String codigoUg,int anoMes,int ordem,Assunto assunto,boolean ativo,long recebidosHistoricos,String hash,Instant recebimento) {}
    public record State(Processing.State situacao,Processing.Diagnostic diagnostico,long processados,long efetivos) {}
    public record LotView(Lot lote,State processamento) {}
    public record Event(long id,long identificadorRemessa,Long numeroLote,String tipo,Instant instante) {}
    public record Record(long indice,byte[] payload) {public Record {payload=payload.clone();}public byte[] payload(){return payload.clone();}}
    public record Counts(Assunto assunto,long lotes,long recebidos,long historicos) {}
    public record ResultCounts(Assunto assunto,String situacao,long lotes,long processados,long efetivos) {}
}
