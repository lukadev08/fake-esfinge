package br.dev.luka.esfinge.remessas.domain;

import java.math.BigDecimal;
import java.util.List;

public record ReceivedContent(byte[] bytes, int records, Assunto subject, List<BigDecimal> creditValues) {
    public ReceivedContent {
        bytes=bytes.clone(); creditValues=List.copyOf(creditValues);
        if (subject==null || records<1 || bytes.length==0
                || (subject==Assunto.LANCAMENTO_CREDITO_TRIBUTARIO ? creditValues.size()!=records : !creditValues.isEmpty())
                || creditValues.stream().anyMatch(v->v.stripTrailingZeros().scale()>2))
            throw new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE);
    }
    @Override public byte[] bytes() { return bytes.clone(); }
}
