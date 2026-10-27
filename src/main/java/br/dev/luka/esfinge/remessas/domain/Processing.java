package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;
import java.util.List;

public final class Processing {
    private Processing() {}
    public enum Mode { MANUAL, AUTOMATICO }
    public enum State { RECEBIDO, EM_PROCESSAMENTO, PROCESSADO_SUCESSO }
    public record Configuration(Mode modo,long latenciaMilissegundos,Instant instante) {}
    public record Started(String situacao,Instant dataEnvio,long identificadorRemessaOnline) {}
    public record Advanced(Instant instante,int passosExecutados) {}
    public record Work(long id,long loteId,long remessaId,String cenario,String ug,long geracao,State state,Instant due,long latencyMillis) {}
    public record Consultation(Lote lote,State state) {}
    public record Snapshot(LoteInspection received,State state,List<byte[]> processed,List<byte[]> effective) {
        public Snapshot { processed=copy(processed);effective=copy(effective); }
        @Override public List<byte[]> processed() { return copy(processed); }
        @Override public List<byte[]> effective() { return copy(effective); }
    }
    public static List<byte[]> copy(List<byte[]> records) { return records.stream().map(byte[]::clone).toList(); }
}
