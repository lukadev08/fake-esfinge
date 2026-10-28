package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.application.InspectionService;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static br.dev.luka.esfinge.remessas.JdbcFinalizationTest.*;

class JdbcInspectionTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper(){var h=new JdbcFinalizationTest();h.temporary=temporary;return h;}
    InspectionService service(Fixture f){var r=new JdbcRemessaRepository(f.source());var p=new JdbcProcessingRepository(f.source());return new InspectionService(JdbcResetTest.inspection(f),r,p,p,new JdbcTransactions(f.source()),f.clock());}
    Inspection.Filter filter(){return new Inspection.Filter(null,null,null,null);}
    @Test void reinicioContinuaKeysetIsolaCenarioUgCompetenciaAssuntoEDetalhe() {
        var h=helper();var a=h.fixture("cenario-a");var b=h.fixture("cenario-b");var s=service(a);String t=a.token("0001"),k=a.remessas().open(t,202601,"NAO").key();long first=a.receive(t,k,SaldoHttpTest.PAYLOAD).number(),second=a.receive(t,k,SaldoHttpTest.PAYLOAD).number();
        String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var other=b.receive(bt,bk,SaldoHttpTest.PAYLOAD);String ug=a.token("0002"),ugKey=a.remessas().open(ug,202602,"NAO").key();a.receive(ug,ugKey,PrazoHttpTest.PAYLOAD);
        var f=new Inspection.Filter(null,"0001",202601,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO);var page=s.lotes(ADMIN,f,1,null);assertEquals(first,page.itens().getFirst().lote().numeroLote());assertNotNull(page.proximoCursor());
        var restarted=service(h.fixture("cenario-a"));var next=restarted.lotes(ADMIN,f,1,page.proximoCursor());assertEquals(second,next.itens().getFirst().lote().numeroLote());assertNull(next.proximoCursor());
        assertEquals(1,s.remessas(ADMIN,f,50,null).itens().size());assertEquals(0,s.lotes(ADMIN,new Inspection.Filter(null,"1",null,null),50,null).itens().size());assertEquals(0,s.lotes(ADMIN,new Inspection.Filter(null,"0001",202602,null),50,null).itens().size());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->s.records(ADMIN,other.number(),Inspection.Type.HISTORICOS,50,null)).kind());assertEquals(2,s.remessa(ADMIN,1).recebimento().getFirst().lotes());
        var remessas=s.remessas(ADMIN,filter(),1,null);assertEquals(1,remessas.itens().size());assertEquals(1,s.remessas(ADMIN,filter(),1,remessas.proximoCursor()).itens().size());
    }
    @ParameterizedTest @ValueSource(strings={"ERRO","DESCARTE","CANCELAMENTO","EXPIRACAO"})
    void historicoEEventosReconciliamRecuperacaoSemReativarDados(String operation) {
        var h=helper();var f=h.fixture("cenario-a");String t=f.token("0001"),k=f.remessas().open(t,202601,"NAO").key();long lot=f.receive(t,k,SaldoHttpTest.PAYLOAD).number();long later=f.receive(t,k,SaldoHttpTest.PAYLOAD).number();
        if(operation.equals("ERRO")){f.processing().planFailure(ADMIN,lot,0);f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);f.processing().start(t,k);f.processing().advance(ADMIN,0,10);}
        else if(operation.equals("DESCARTE")){h.complete(f,t,k);f.processing().discard(t,k,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO);}
        else if(operation.equals("CANCELAMENTO"))f.processing().cancel(t,k);
        else f.processing().advance(ADMIN,259200000,1);
        var s=service(f);var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);
        for(long number:new long[]{lot,later}){
            assertEquals(0,s.records(ADMIN,number,Inspection.Type.RECEBIDOS,1,null).itens().size());assertEquals(0,s.records(ADMIN,number,Inspection.Type.PROCESSADOS,1,null).itens().size());assertEquals(0,s.records(ADMIN,number,Inspection.Type.EFETIVOS,1,null).itens().size());assertEquals(0,s.records(ADMIN,number,Inspection.Type.HISTORICOS,1,null).itens().getFirst().indice());
        }
        var page=s.lotes(ADMIN,filter(),1,null);assertFalse(page.itens().getFirst().lote().ativo());assertNotNull(page.proximoCursor());assertFalse(s.lotes(ADMIN,filter(),1,page.proximoCursor()).itens().getFirst().lote().ativo());
        if(operation.equals("ERRO")){assertEquals(Processing.State.LOTE_DESCARTADO,page.itens().getFirst().processamento().situacao());assertEquals(Processing.State.ABORTADO_ERRO_LOTE_ANTERIOR,s.lotes(ADMIN,filter(),1,page.proximoCursor()).itens().getFirst().processamento().situacao());}
        var events=new ArrayList<Inspection.Event>();String cursor=null;do{var p=s.eventos(ADMIN,1,1,cursor,"remessa");events.addAll(p.itens());cursor=p.proximoCursor();}while(cursor!=null);
        assertEquals(new JdbcRemessaRepository(f.source()).events(1).size(),events.size());for(int i=1;i<events.size();i++)assertTrue(events.get(i).id()>events.get(i-1).id());assertTrue(events.stream().anyMatch(e->Objects.equals(e.numeroLote(),lot)));
        var report=s.report(ADMIN,1,null,"FICTICIA");assertEquals(0,report.recebimento().getFirst().recebidos());assertEquals(2,report.recebimento().getFirst().historicos());assertEquals(2,s.lotes(ADMIN,new Inspection.Filter(null,"0001",202601,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO),50,null).itens().size());
        if(operation.equals("EXPIRACAO"))assertNotNull(s.remessa(ADMIN,1).remessa().expiredAt());s.remessa(ADMIN,1);assertEquals(before,snapshot(jdbc));
    }
    @Test void lerComTrabalhoPendenteOuPrazoVencidoNaoExecutaNemRenovaOuExpira() {
        var f=helper().fixture("cenario-a");String t=f.token("0001"),k=f.remessas().open(t,202601,"NAO").key();long lot=f.receive(t,k,PrazoHttpTest.PAYLOAD).number();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);f.processing().start(t,k);
        new JdbcTransactions(f.source()).execute(()->{f.clock().advance(259200000);return null;});var jdbc=new JdbcTemplate(f.source());var before=snapshot(jdbc);var instant=f.clock().instant();var s=service(f);
        s.lotes(ADMIN,filter(),1,null);s.remessas(ADMIN,filter(),1,null);s.remessa(ADMIN,1);s.eventos(ADMIN,1,1,null,"remessa");for(var type:Inspection.Type.values())s.records(ADMIN,lot,type,1,null);s.report(ADMIN,1,null,"NAO_INFORMADA");
        assertEquals(before,snapshot(jdbc));assertEquals(instant,f.clock().instant());assertNull(s.remessa(ADMIN,1).remessa().expiredAt());assertEquals(Processing.State.RECEBIDO,s.lotes(ADMIN,filter(),1,null).itens().getFirst().processamento().situacao());
    }
    @Test void indicesProcessadosNaoSaoReindexadosENullAusentePermanecemDistintos() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String t=f.token("0001"),k=f.remessas().open(t,202601,"NAO").key();String payload="{\"saldosIniciaisCreditoTributario\":["+SaldoHttpTest.RECORD+","+SaldoHttpTest.RECORD.substring(0,SaldoHttpTest.RECORD.length()-1)+",\"tipoSaldoInicial\":null}]}";long lot=f.receive(t,k,payload).number();h.complete(f,t,k);
        var jdbc=new JdbcTemplate(f.source());jdbc.update("DELETE FROM registros_efetivos WHERE lote_id=? AND indice=0",lot);jdbc.update("DELETE FROM registros_processados WHERE lote_id=? AND indice=0",lot);var s=service(f);var mapper=new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        var processed=s.records(ADMIN,lot,Inspection.Type.PROCESSADOS,1,null);assertEquals(1,processed.itens().getFirst().indice());assertTrue(mapper.readTree(processed.itens().getFirst().payload()).get("tipoSaldoInicial").isNull());
        var received=s.records(ADMIN,lot,Inspection.Type.RECEBIDOS,1,null);assertFalse(mapper.readTree(received.itens().getFirst().payload()).has("tipoSaldoInicial"));var last=s.records(ADMIN,lot,Inspection.Type.RECEBIDOS,1,received.proximoCursor());assertTrue(mapper.readTree(last.itens().getFirst().payload()).get("tipoSaldoInicial").isNull());
        assertEquals(new java.math.BigDecimal("123456789012345678901234567890.12"),mapper.readTree(last.itens().getFirst().payload()).get("saldoPrincipal").decimalValue());
    }
    @Test void remocaoEntrePaginasRetornaVazioSemInventarSnapshotNemReindexar() {
        var h=helper();var f=h.fixture("cenario-a");String t=f.token("0001"),k=f.remessas().open(t,202601,"NAO").key();long lot=f.receive(t,k,"{\"saldosIniciaisCreditoTributario\":["+SaldoHttpTest.RECORD+","+SaldoHttpTest.RECORD+"]}").number();h.complete(f,t,k);var s=service(f);
        var first=s.records(ADMIN,lot,Inspection.Type.EFETIVOS,1,null);assertNotNull(first.proximoCursor());f.processing().discard(t,k,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO);assertTrue(s.records(ADMIN,lot,Inspection.Type.EFETIVOS,1,first.proximoCursor()).itens().isEmpty());assertEquals(1,s.records(ADMIN,lot,Inspection.Type.HISTORICOS,1,null).itens().size());
    }

    @Test void contagensNaoMultiplicamValoresMonetariosEventosOuRegistrosDuplicados() {
        var h=helper();var f=h.fixture("cenario-a");String t=f.token("0001"),k=f.remessas().open(t,202601,"NAO").key();String payload="{\"saldosIniciaisCreditoTributario\":["+SaldoHttpTest.RECORD+","+SaldoHttpTest.RECORD+"]}";
        long first=f.receive(t,k,payload).number();f.receive(t,k,payload);h.complete(f,t,k);var s=service(f);var jdbc=new JdbcTemplate(f.source());
        assertEquals(16,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos",Integer.class)>4);
        var report=s.report(ADMIN,50,null,"FICTICIA");assertEquals(2,report.recebimento().getFirst().lotes());assertEquals(4,report.recebimento().getFirst().recebidos());assertEquals(4,report.recebimento().getFirst().historicos());
        assertEquals(2,report.resultados().getFirst().lotes());assertEquals(4,report.resultados().getFirst().processados());assertEquals(4,report.resultados().getFirst().efetivos());
        var lot=s.lotes(ADMIN,filter(),1,null).itens().getFirst();assertEquals(first,lot.lote().numeroLote());assertEquals(2,lot.lote().recebidosHistoricos());assertEquals(2,lot.processamento().processados());assertEquals(2,lot.processamento().efetivos());
        var detail=s.remessa(ADMIN,1);assertEquals(report.recebimento(),detail.recebimento());assertEquals(report.resultados(),detail.processamento());
        f.processing().discard(t,k,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO);var after=s.report(ADMIN,50,null,"FICTICIA");assertEquals(2,after.recebimento().getFirst().lotes());assertEquals(0,after.recebimento().getFirst().recebidos());assertEquals(4,after.recebimento().getFirst().historicos());assertEquals(0,after.resultados().getFirst().processados());assertEquals(0,after.resultados().getFirst().efetivos());
        assertEquals(2,s.lotes(ADMIN,new Inspection.Filter(null,"0001",202601,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO),50,null).itens().size());f.processing().reset(ADMIN,"cenario-a",0);
        assertTrue(s.lotes(ADMIN,filter(),50,null).itens().isEmpty());assertTrue(s.report(ADMIN,50,null,"FICTICIA").recebimento().isEmpty());assertEquals(1,s.report(ADMIN,50,null,"FICTICIA").configuracao().generation());
    }
}
