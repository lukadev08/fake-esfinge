package br.dev.luka.esfinge.remessas.domain;

import java.math.BigDecimal;
import java.util.*;

public record ReceivedContent(byte[] bytes, int records, Assunto subject, List<Map<String,BigDecimal>> monetaryValues) {
    public ReceivedContent {
        if (bytes==null || bytes.length==0 || records<1 || subject==null || monetaryValues==null) throw invalid();
        Set<String> fields=switch(subject) {
            case CADASTRO_CONTRIBUINTE -> Set.of();
            case LANCAMENTO_CREDITO_TRIBUTARIO -> Set.of("valorLancamentoCreditoTributario");
            case DIARIO_ARRECADACAO -> Set.of("valorCreditoTributario");
            case BAIXA_CREDITO_TRIBUTARIO -> Set.of("valorOperacaoBaixa");
            case SALDO_INICIAL_CREDITO_TRIBUTARIO -> Set.of("saldoPrincipal","saldoMulta","saldoJuros","saldoAtualizacaoMonetaria");
        };
        if (fields.isEmpty() ? !monetaryValues.isEmpty() : monetaryValues.size()!=records) throw invalid();
        for (var values:monetaryValues) {
            if (values==null || !values.keySet().equals(fields)
                    || values.values().stream().anyMatch(v->v==null || v.stripTrailingZeros().scale()>2)) throw invalid();
        }
        bytes=bytes.clone(); monetaryValues=monetaryValues.stream().map(Map::copyOf).toList();
    }
    @Override public byte[] bytes() { return bytes.clone(); }
    private static RemessaFailure invalid() { return new RemessaFailure(RemessaFailure.Kind.INVALID_STRUCTURE); }
}
