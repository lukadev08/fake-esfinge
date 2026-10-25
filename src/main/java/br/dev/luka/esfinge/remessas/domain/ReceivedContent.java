package br.dev.luka.esfinge.remessas.domain;

import java.math.BigDecimal;
import java.util.List;

public record ReceivedContent(byte[] bytes, int records, Assunto subject, List<BigDecimal> monetaryValues) {
    public ReceivedContent {
        bytes=bytes.clone(); monetaryValues=List.copyOf(monetaryValues);
        if (subject==null || records<1 || bytes.length==0
                || (subject==Assunto.CADASTRO_CONTRIBUINTE ? !monetaryValues.isEmpty() : monetaryValues.size()!=records)
                || monetaryValues.stream().anyMatch(v->v.stripTrailingZeros().scale()>2))
            throw new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE);
    }
    @Override public byte[] bytes() { return bytes.clone(); }
}
