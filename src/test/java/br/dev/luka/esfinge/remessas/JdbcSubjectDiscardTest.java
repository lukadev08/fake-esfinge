package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
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

class JdbcSubjectDiscardTest {
    @TempDir Path temporary;
    JdbcFinalizationTest helper() {var h=new JdbcFinalizationTest();h.temporary=temporary;return h;}
    static String payload(Assunto subject) {return switch(subject) {
        case CADASTRO_CONTRIBUINTE->RemessaHttpTest.PAYLOAD;case LANCAMENTO_CREDITO_TRIBUTARIO->CreditoHttpTest.PAYLOAD;
        case DIARIO_ARRECADACAO->ArrecadacaoHttpTest.PAYLOAD;case BAIXA_CREDITO_TRIBUTARIO->BaixaHttpTest.PAYLOAD;
        case SALDO_INICIAL_CREDITO_TRIBUTARIO->SaldoHttpTest.PAYLOAD;case INICIO_PRAZO_PRESCRICIONAL->PrazoHttpTest.PAYLOAD;};}
    @ParameterizedTest @EnumSource(Assunto.class)
    void seisAssuntosRemovemTodosAtivosPreservamHistoricosDecimaisEIsolamento(Assunto subject) throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();
        var json=new com.fasterxml.jackson.databind.ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        var root=json.readTree(payload(subject));var records=(com.fasterxml.jackson.databind.node.ArrayNode)root.elements().next();records.add(records.get(0).deepCopy());String doubled=root.toString();
        var done=a.receive(token,key,doubled);h.complete(a,token,key);var pending=a.receive(token,key,doubled);a.processing().planFailure(ADMIN,pending.number(),1);
        var otherSubject=Assunto.values()[(subject.ordinal()+1)%6];var other=a.receive(token,key,payload(otherSubject));
        String ug=a.token("0002"),uk=a.remessas().open(ug,202601,"NAO").key(),next=a.remessas().open(token,202602,"NAO").key();var ul=a.receive(ug,uk,doubled);var nl=a.receive(token,next,doubled);
        var b=h.fixture("cenario-b");String bt=b.token("0001"),bk=b.remessas().open(bt,202601,"NAO").key();var bl=b.receive(bt,bk,doubled);
        var untouched=a.processing().inspect(ADMIN,other.number());var jdbc=new JdbcTemplate(a.source());
        var version=jdbc.queryForList("SELECT * FROM schema_migrations");var schema=jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name");
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.processing().discard(ug,key,subject)).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.processing().discard(bt,key,subject)).kind());
        a.processing().advance(ADMIN,123,1);a.processing().discard(token,key,subject);
        for(var lot:List.of(done,pending)) {var saved=a.processing().inspect(ADMIN,lot.number());assertFalse(saved.active());assertTrue(saved.processed().isEmpty());assertTrue(saved.effective().isEmpty());assertArrayEquals(lot.bytes(),saved.received().lote().bytes());assertEquals(lot.hash(),saved.received().lote().hash());assertEquals(2,saved.received().lote().records());}
        assertEquals(Processing.State.PROCESSADO_SUCESSO,a.processing().inspect(ADMIN,done.number()).state());assertNull(a.processing().inspect(ADMIN,done.number()).diagnostic());
        var discarded=a.processing().inspect(ADMIN,pending.number());assertEquals(Processing.State.LOTE_DESCARTADO,discarded.state());assertEquals("SIM_DESCARTE_ASSUNTO",discarded.diagnostic().codigo());assertNull(discarded.diagnostic().registro());
        assertEquals(1,a.processing().failurePlan(ADMIN,pending.number()).registro());assertStable(untouched,a.processing().inspect(ADMIN,other.number()));for(var lot:List.of(ul,nl))assertTrue(a.processing().inspect(ADMIN,lot.number()).active());assertTrue(b.processing().inspect(ADMIN,bl.number()).active());
        var saved=snapshot(jdbc);var restarted=h.fixture("cenario-a");assertEquals(saved,snapshot(jdbc));assertEquals(version,jdbc.queryForList("SELECT * FROM schema_migrations"));assertEquals(schema,jdbc.queryForList("SELECT * FROM sqlite_master ORDER BY name"));assertEquals(17,jdbc.queryForObject("SELECT COUNT(*) FROM sqlite_master WHERE type='table'",Integer.class));
        restarted.processing().discard(token,key,subject);assertStable(discarded,restarted.processing().inspect(ADMIN,pending.number()));
        var replacement=restarted.receive(token,key,doubled);assertTrue(replacement.number()>pending.number());assertTrue(replacement.order()>pending.order());assertEquals(done.hash(),replacement.hash());
        h.complete(restarted,token,key);var result=restarted.processing().inspect(ADMIN,replacement.number());assertEquals(Processing.State.PROCESSADO_SUCESSO,result.state());assertEquals(2,result.effective().size());assertEquals(root.elements().next(),json.readTree("["+new String(result.effective().get(0),java.nio.charset.StandardCharsets.UTF_8)+","+new String(result.effective().get(1),java.nio.charset.StandardCharsets.UTF_8)+"]"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM valores_recebidos WHERE lote_id IN (?,?)",Integer.class,done.number(),pending.number()));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void dezMilEUmRegistrosRemovidosComMultiplicidadeSemDeduplicarReenvio() throws Exception {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();
        var json=new com.fasterxml.jackson.databind.ObjectMapper();String record=json.readTree(RemessaHttpTest.PAYLOAD).get("cadastrosContribuintes").get(0).toString();String payload="{\"cadastrosContribuintes\":["+String.join(",",Collections.nCopies(10001,record))+"]}";
        var first=f.receive(token,key,payload);h.complete(f,token,key);f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.remessas().finish(token,key,totals(0))).kind());
        var second=f.receive(token,key,payload);h.complete(f,token,key);f.remessas().finish(token,key,totals(10001));assertEquals(first.hash(),second.hash());assertEquals(10001,f.processing().inspect(ADMIN,second.number()).effective().size());assertTrue(f.processing().inspect(ADMIN,first.number()).effective().isEmpty());
    }
    @Test void chaveInteiraBloqueiaAgendadoEEmOutroAssuntoMasOutraChaveNaoBloqueia() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();f.processing().configure(ADMIN,Processing.Mode.MANUAL,0);
        var target=f.receive(token,key,RemessaHttpTest.PAYLOAD);var other=f.receive(token,key,SaldoHttpTest.PAYLOAD);f.processing().start(token,key);var jdbc=new JdbcTemplate(f.source());
        for(int step=0;step<4;step++) {var before=snapshot(jdbc);assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE)).kind());assertEquals(before,snapshot(jdbc));f.processing().advance(ADMIN,0,1);}
        String another=f.remessas().open(token,202602,"NAO").key();f.receive(token,another,RemessaHttpTest.PAYLOAD);f.processing().start(token,another);f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);assertFalse(f.processing().inspect(ADMIN,target.number()).active());assertTrue(f.processing().inspect(ADMIN,other.number()).active());
    }
    @Test void assuntoSemDadosERepeticaoRenovamAtividadeSemReescreverControle() {
        var h=helper();var f=h.fixture("cenario-a");String token=f.token("0001"),key=f.remessas().open(token,202601,"NAO").key();var lot=f.receive(token,key,RemessaHttpTest.PAYLOAD);var jdbc=new JdbcTemplate(f.source());
        f.processing().advance(ADMIN,1000,1);f.processing().discard(token,key,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO);assertEquals(INITIAL.plusSeconds(1).toString(),jdbc.queryForObject("SELECT atividade FROM remessas",String.class));assertTrue(f.processing().inspect(ADMIN,lot.number()).active());
        f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);var old=f.processing().inspect(ADMIN,lot.number());f.processing().advance(ADMIN,1000,1);f.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);assertStable(old,f.processing().inspect(ADMIN,lot.number()));assertEquals(INITIAL.plusSeconds(2).toString(),jdbc.queryForObject("SELECT atividade FROM remessas",String.class));assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo LIKE 'ASSUNTO_DESCARTADO_%'",Integer.class));
    }
    static void assertStable(Processing.Snapshot before,Processing.Snapshot after) {
        assertEquals(before.state(),after.state());assertEquals(before.active(),after.active());assertEquals(before.diagnostic(),after.diagnostic());assertEquals(before.received().lote().hash(),after.received().lote().hash());assertArrayEquals(before.received().lote().bytes(),after.received().lote().bytes());
        assertEquals(before.processed().stream().map(HexFormat.of()::formatHex).toList(),after.processed().stream().map(HexFormat.of()::formatHex).toList());assertEquals(before.effective().stream().map(HexFormat.of()::formatHex).toList(),after.effective().stream().map(HexFormat.of()::formatHex).toList());
        long id=before.received().lote().number();assertEquals(before.received().events().stream().filter(e->Objects.equals(e.lote(),id)).toList(),after.received().events().stream().filter(e->Objects.equals(e.lote(),id)).toList());
    }
    @ParameterizedTest @ValueSource(strings={"receive","start","finish","discard","complete"})
    void concorrenciaDuasInstanciasLinearizaSemEfeitoTardio(String action) throws Exception {
        var h=helper();var a=h.fixture("cenario-a");String token=a.token("0001"),key=a.remessas().open(token,202601,"NAO").key();a.processing().configure(ADMIN,Processing.Mode.MANUAL,0);var lot=a.receive(token,key,RemessaHttpTest.PAYLOAD);
        if(action.equals("finish"))h.complete(a,token,key);
        if(action.equals("complete")){a.processing().start(token,key);a.processing().advance(ADMIN,0,1);}
        var b=h.fixture("cenario-a");var result=race(()->{a.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);return "discarded";},()->{switch(action){
            case "receive"->b.receive(token,key,RemessaHttpTest.PAYLOAD);
            case "start"->b.processing().start(token,key);
            case "finish"->b.remessas().finish(token,key,totals(1));
            case "discard"->b.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);
            default->b.processing().advance(ADMIN,0,1);
        }return action;});
        assertTrue(result.contains("discarded") || result.contains(action));var jdbc=new JdbcTemplate(a.source());
        if(action.equals("finish")){assertEquals(1,result.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());if(result.contains("finish"))assertTrue(a.processing().inspect(ADMIN,lot.number()).active());else assertFalse(a.processing().inspect(ADMIN,lot.number()).active());}
        if(action.equals("start")){assertEquals(1,result.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());if(result.contains("start")){assertTrue(a.processing().inspect(ADMIN,lot.number()).active());a.processing().advance(ADMIN,0,10);a.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);}}
        if(action.equals("complete")){assertTrue(result.contains("complete"));if(!result.contains("discarded"))assertTrue(result.contains(RemessaFailure.Kind.CONFLICT));a.processing().discard(token,key,Assunto.CADASTRO_CONTRIBUINTE);}
        if(action.equals("receive")){assertTrue(result.containsAll(List.of("discarded","receive")));assertFalse(a.processing().inspect(ADMIN,lot.number()).active());assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM lotes WHERE ativo=1",Integer.class)<=1);}
        if(action.equals("discard")){assertTrue(result.containsAll(List.of("discarded","discard")));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='LOTE_DESCARTADO'",Integer.class));}
        var repo=new JdbcProcessingRepository(a.source());var tx=new JdbcTransactions(a.source());
        var stale=new Processing.Work(1,lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),0,Processing.State.EM_PROCESSAMENTO,INITIAL,0);
        if(!a.processing().inspect(ADMIN,lot.number()).active())assertFalse(tx.execute(()->repo.complete(stale,new StructuralRecords().process(lot),INITIAL)));
        var restarted=h.fixture("cenario-a");var before=snapshot(jdbc);assertEquals(0,restarted.processing().advance(ADMIN,0,100).passosExecutados());assertEquals(before,snapshot(jdbc));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
}
