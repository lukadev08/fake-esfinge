package br.dev.luka.esfinge.remessas.application;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;
import br.dev.luka.esfinge.remessas.application.port.in.InspectExecution;
import br.dev.luka.esfinge.remessas.application.port.out.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.io.*;
import java.time.Clock;
import java.util.*;
import java.util.function.ToLongFunction;
import static br.dev.luka.esfinge.remessas.domain.RemessaFailure.Kind.*;

public final class InspectionService implements InspectExecution {
    private final InspectScenario scenario;
    private final InspectionRemessas remessas;
    private final InspectionProcessing results;
    private final ProcessingRepository processing;
    private final Transactions transactions;
    private final Clock clock;
    public InspectionService(InspectScenario scenario,InspectionRemessas remessas,InspectionProcessing results,ProcessingRepository processing,Transactions transactions,Clock clock) {
        this.scenario=scenario;this.remessas=remessas;this.results=results;this.processing=processing;this.transactions=transactions;this.clock=clock;
    }
    public ScenarioSnapshot authorize(String key){return scenario.inspect(key);}
    private Inspection.Filter filter(ScenarioSnapshot s,Inspection.Filter f) {
        if(f==null || f.codigoUg()!=null && f.codigoUg().isEmpty() || f.anoMes()!=null && (f.anoMes()<202601 || f.anoMes()>202612)) throw new RemessaFailure(INVALID_REQUEST);
        if(f.cenario()!=null && !s.scenario().equals(f.cenario())) throw new RemessaFailure(NOT_FOUND);
        return new Inspection.Filter(s.scenario(),f.codigoUg(),f.anoMes(),f.assunto());
    }
    private String binding(Inspection.Filter f){return field(f.cenario())+field(f.codigoUg())+field(f.anoMes()==null?null:f.anoMes().toString())+field(f.assunto()==null?null:f.assunto().name());}
    private String field(String value){return value==null?"-:":value.length()+":"+value;}
    private record Cursor(long generation,long after,long ceiling,int limit,String resource,String binding) {}
    private String encode(Cursor c) {
        try {var bytes=new ByteArrayOutputStream();var out=new DataOutputStream(bytes);out.writeInt(1);out.writeLong(c.generation());out.writeLong(c.after());out.writeLong(c.ceiling());out.writeInt(c.limit());out.writeUTF(c.resource());out.writeUTF(c.binding());return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray());}
        catch(IOException failure){throw new IllegalStateException(failure);}
    }
    private Cursor cursor(ScenarioSnapshot s,String resource,long id,int limit,String value,String binding,boolean records) {
        if(limit<1 || limit>200) throw new RemessaFailure(INVALID_REQUEST);
        if(value==null) return new Cursor(s.generation(),records?-1:0,remessas.ceiling(s.scenario(),resource,id),limit,resource+":"+id,binding);
        try {
            if(value.length()>4096) throw new IOException();
            var in=new DataInputStream(new ByteArrayInputStream(Base64.getUrlDecoder().decode(value)));
            if(in.readInt()!=1) throw new IOException();
            var c=new Cursor(in.readLong(),in.readLong(),in.readLong(),in.readInt(),in.readUTF(),in.readUTF());
            if(in.available()!=0 || !encode(c).equals(value) || c.after()<(records?-1:0) || c.ceiling()<(records?-1:0) || c.after()>c.ceiling() || c.generation()<0
                || c.limit()!=limit || !c.resource().equals(resource+":"+id) || !c.binding().equals(binding)) throw new IOException();
            if(c.generation()!=s.generation()) throw new RemessaFailure(CONFLICT);
            return c;
        } catch(IOException|IllegalArgumentException failure){throw new RemessaFailure(INVALID_REQUEST);}
    }
    private Inspection.Window window(Cursor c){return new Inspection.Window(c.after(),c.ceiling(),c.limit()+1);}
    private <T> Inspection.Page<T> page(List<T> rows,Cursor c,ToLongFunction<T> id) {
        boolean more=rows.size()>c.limit();var items=more?rows.subList(0,c.limit()):rows;
        String next=more?encode(new Cursor(c.generation(),id.applyAsLong(items.getLast()),c.ceiling(),c.limit(),c.resource(),c.binding())):null;
        return new Inspection.Page<>(items,next,c.generation(),c.ceiling());
    }
    public Inspection.Page<Remessa> remessas(String key,Inspection.Filter f,int limit,String value) {
        authorize(key);return transactions.execute(()->{var s=authorize(key);var normalized=filter(s,f);var c=cursor(s,"remessas",0,limit,value,binding(normalized),false);return page(remessas.remessas(normalized,window(c)),c,Remessa::id);});
    }
    public Inspection.Page<Inspection.LotView> lotes(String key,Inspection.Filter f,int limit,String value) {
        authorize(key);return transactions.execute(()->{var s=authorize(key);var normalized=filter(s,f);var c=cursor(s,"lotes",0,limit,value,binding(normalized),false);
            var lots=page(remessas.lotes(normalized,window(c)),c,Inspection.Lot::numeroLote);
            return new Inspection.Page<>(lots.itens().stream().map(l->new Inspection.LotView(l,results.summary(s.scenario(),l.numeroLote()))).toList(),lots.proximoCursor(),lots.geracao(),lots.teto());});
    }
    private Remessa requireRemessa(String s,long id){if(id<=0)throw new RemessaFailure(INVALID_REQUEST);return remessas.remessa(s,id).orElseThrow(()->new RemessaFailure(NOT_FOUND));}
    public RemessaDetail remessa(String key,long id) {
        authorize(key);return transactions.execute(()->{var s=authorize(key);var r=requireRemessa(s.scenario(),id);return new RemessaDetail(r,remessas.counts(s.scenario(),id),results.counts(s.scenario(),id));});
    }
    public Inspection.Page<Inspection.Event> eventos(String key,long id,int limit,String value,String binding) {
        authorize(key);return transactions.execute(()->{var s=authorize(key);if(id<0)throw new RemessaFailure(INVALID_REQUEST);var c=cursor(s,"eventos",id,limit,value,binding,false);
            if(id>0)requireRemessa(s.scenario(),id);return page(remessas.eventos(s.scenario(),id,window(c)),c,Inspection.Event::id);});
    }
    public Inspection.Page<Inspection.Record> records(String key,long id,Inspection.Type type,int limit,String value) {
        authorize(key);return transactions.execute(()->{var s=authorize(key);if(id<=0 || type==null)throw new RemessaFailure(INVALID_REQUEST);
            var c=cursor(s,"registros",id,limit,value,type.name(),true);remessas.lote(s.scenario(),id).orElseThrow(()->new RemessaFailure(NOT_FOUND));
            var rows=type==Inspection.Type.RECEBIDOS || type==Inspection.Type.HISTORICOS?remessas.recebidos(s.scenario(),id,type==Inspection.Type.HISTORICOS,window(c)):results.records(s.scenario(),id,type,window(c));
            return page(rows,c,Inspection.Record::indice);});
    }
    public Report report(String key,int limit,String value,String origin) {
        authorize(key);if(!Set.of("FICTICIA","SANITIZADA","ORIGINAL","NAO_INFORMADA").contains(origin==null?"":origin))throw new RemessaFailure(INVALID_REQUEST);
        return transactions.execute(()->{var s=authorize(key);var c=cursor(s,"eventos",0,limit,value,"relatorio:"+origin,false);
            return new Report(s,processing.configuration(s.scenario(),clock.instant()),remessas.counts(s.scenario(),null),results.counts(s.scenario(),null),page(remessas.eventos(s.scenario(),0,window(c)),c,Inspection.Event::id));});
    }
}
