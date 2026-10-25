package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.remessas.domain.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReceivedContentTest {
    Map<String,BigDecimal> balance() { return Map.of("saldoPrincipal",new BigDecimal("1.00"),"saldoMulta",new BigDecimal("2.30"),"saldoJuros",new BigDecimal("4.56"),"saldoAtualizacaoMonetaria",new BigDecimal("7.89")); }
    @Test void copiaBytesListaEMapasSemPermitirMutacaoPosterior() {
        byte[] bytes={1,2};var values=new HashMap<>(balance());var list=new ArrayList<Map<String,BigDecimal>>();list.add(values);
        var content=new ReceivedContent(bytes,1,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,list);
        bytes[0]=9;values.put("saldoPrincipal",BigDecimal.TEN);list.clear();byte[] exposed=content.bytes();exposed[0]=8;
        assertArrayEquals(new byte[]{1,2},content.bytes());assertEquals(new BigDecimal("1.00"),content.monetaryValues().getFirst().get("saldoPrincipal"));
        assertThrows(UnsupportedOperationException.class,()->content.monetaryValues().clear());
        assertThrows(UnsupportedOperationException.class,()->content.monetaryValues().getFirst().put("saldoPrincipal",BigDecimal.ZERO));
    }
    @Test void exigeCamposExatosCardinalidadeEPrecisaoPorAssunto() {
        var fields=Map.of(Assunto.LANCAMENTO_CREDITO_TRIBUTARIO,"valorLancamentoCreditoTributario",Assunto.DIARIO_ARRECADACAO,"valorCreditoTributario",Assunto.BAIXA_CREDITO_TRIBUTARIO,"valorOperacaoBaixa");
        for(var subject:fields.entrySet()) {
            assertNotNull(new ReceivedContent(new byte[]{1},1,subject.getKey(),List.of(Map.of(subject.getValue(),BigDecimal.ONE))));
            assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,subject.getKey(),List.of(Map.of("incorreto",BigDecimal.ONE))));
        }
        assertNotNull(new ReceivedContent(new byte[]{1},1,Assunto.CADASTRO_CONTRIBUINTE,List.of()));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.CADASTRO_CONTRIBUINTE,List.of(balance())));
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},2,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,List.of(balance())));
        var missing=new HashMap<>(balance());missing.remove("saldoMulta");
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,List.of(missing)));
        var extra=new HashMap<>(balance());extra.put("extra",BigDecimal.ZERO);
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,List.of(extra)));
        for(String field:balance().keySet()) {
            var invalid=new HashMap<>(balance());invalid.put(field,new BigDecimal("1.001"));
            assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,List.of(invalid)));
        }
        var nullable=new HashMap<>(balance());nullable.put("saldoPrincipal",null);
        assertThrows(RemessaFailure.class,()->new ReceivedContent(new byte[]{1},1,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,List.of(nullable)));
    }
}
