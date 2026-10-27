package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.remessas.domain.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FinalizationTest {
    EnumMap<Assunto,BigInteger> totals() {
        var totals=new EnumMap<Assunto,BigInteger>(Assunto.class);for(var subject:Assunto.values()) totals.put(subject,BigInteger.ZERO);return totals;
    }
    @Test void totaisExatosImutaveisSemOverflow() {
        var totals=totals();totals.put(Assunto.CADASTRO_CONTRIBUINTE,BigInteger.ONE);var command=new Finalization(totals);totals.clear();
        assertDoesNotThrow(()->command.verify(new Finalization.State(1,1,Map.of(Assunto.CADASTRO_CONTRIBUINTE,1L))));
        assertThrows(UnsupportedOperationException.class,()->command.totals().clear());
        var huge=totals();huge.put(Assunto.CADASTRO_CONTRIBUINTE,new BigInteger("9999999999999999999999999999999999999999999"));
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->new Finalization(huge).verify(new Finalization.State(1,1,Map.of(Assunto.CADASTRO_CONTRIBUINTE,1L)))).kind());
    }
    @Test void nucleoRecusaTotaisIncompletosNegativosOuNulos() {
        assertThrows(RemessaFailure.class,()->new Finalization(null));assertThrows(RemessaFailure.class,()->new Finalization(Map.of()));
        for(BigInteger invalid:Arrays.asList(null,BigInteger.valueOf(-1))) {var totals=totals();totals.put(Assunto.CADASTRO_CONTRIBUINTE,invalid);assertThrows(RemessaFailure.class,()->new Finalization(totals));}
    }
    @Test void lotesObrigatoriosTodosConcluidosETotaisPorAssunto() {
        var command=new Finalization(totals());
        for(var state:List.of(new Finalization.State(0,0,Map.of()),new Finalization.State(2,1,Map.of()),new Finalization.State(1,1,Map.of(Assunto.BAIXA_CREDITO_TRIBUTARIO,1L))))
            assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->command.verify(state)).kind());
    }
}
