package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;

public record Lote(long number,long remessaId,String key,String scenario,String ug,int yearMonth,int order,int records,Assunto subject,String hash,byte[] bytes,Instant receivedAt) {
    public Lote { bytes=bytes.clone(); }
    @Override public byte[] bytes() { return bytes.clone(); }
}
