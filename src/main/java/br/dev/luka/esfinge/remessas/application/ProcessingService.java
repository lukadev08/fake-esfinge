package br.dev.luka.esfinge.remessas.application;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.remessas.application.port.in.*;
import br.dev.luka.esfinge.remessas.application.port.out.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.*;
import static br.dev.luka.esfinge.remessas.domain.RemessaFailure.Kind.*;

public final class ProcessingService implements Processamento {
    private final Authentication auth;
    private final InspectScenario inspection;
    private final Remessas remessas;
    private final RemessaRepository lots;
    private final ProcessingRepository repository;
    private final Transactions transactions;
    private final Clock clock;
    private final BusinessTime time;
    private final ProcessRecords processor;
    private final String scenario;
    public ProcessingService(Authentication auth,InspectScenario inspection,Remessas remessas,RemessaRepository lots,ProcessingRepository repository,Transactions transactions,Clock clock,BusinessTime time,ProcessRecords processor,String scenario) {
        this.auth=auth;this.inspection=inspection;this.remessas=remessas;this.lots=lots;this.repository=repository;this.transactions=transactions;this.clock=clock;this.time=time;this.processor=processor;this.scenario=scenario;
    }
    public synchronized Processing.Started start(String token,String key) {
        var session=auth.authenticate(token);if(key==null || key.isEmpty()) throw new RemessaFailure(INVALID_REQUEST);
        return transactions.execute(()->{
            var remessa=lots.find(key,session.scenario(),session.ug()).orElseThrow(()->new RemessaFailure(NOT_FOUND));
            remessa.requireOpen();
            long generation=repository.generation(session.scenario());
            if(repository.active(remessa.id(),generation)) throw new RemessaFailure(CONFLICT);
            var pending=repository.eligible(remessa);if(pending.isEmpty()) throw new RemessaFailure(CONFLICT);
            var now=clock.instant();var config=repository.configuration(session.scenario(),now);
            try {
                var due=now;time.validate(due);
                for(int i=0;i<pending.size();i++) {due=due.plusMillis(config.latenciaMilissegundos());time.validate(due);}
                repository.schedule(remessa,pending,generation,now,config.latenciaMilissegundos());
            }
            catch(ArithmeticException|DateTimeException failure) {throw new RemessaFailure(INVALID_REQUEST);}
            return new Processing.Started("RECEBIDO",now,remessa.id());
        });
    }
    public synchronized void discard(String token,String key,Assunto subject) {
        var session=auth.authenticate(token);if(key==null || key.isEmpty() || subject==null) throw new RemessaFailure(INVALID_REQUEST);
        transactions.execute(()->{
            var remessa=lots.find(key,session.scenario(),session.ug()).orElseThrow(()->new RemessaFailure(NOT_FOUND));
            remessa.requireOpen();long generation=repository.generation(session.scenario());
            if(repository.active(remessa.id(),generation)) throw new RemessaFailure(CONFLICT);
            repository.discard(remessa,subject,generation,clock.instant());return null;
        });
    }
    public Processing.Consultation consult(String token,long number) {
        return transactions.execute(()->{var lot=remessas.consult(token,number);return new Processing.Consultation(lot,repository.state(lot.number()),repository.diagnostic(lot.number()));});
    }
    public Processing.Snapshot inspect(String adminKey,long number) {
        return transactions.execute(()->{var received=remessas.inspect(adminKey,number);long lot=received.lote().number();return new Processing.Snapshot(received,repository.state(lot),repository.processed(lot),repository.effective(lot),repository.isActive(lot),repository.diagnostic(lot));});
    }
    public Processing.FailurePlan failurePlan(String key,long number) {
        authorize(key);return transactions.execute(()->{var lot=remessas.inspect(key,number).lote();return repository.failurePlan(lot.number(),scenario,repository.generation(scenario)).orElseThrow(()->new RemessaFailure(NOT_FOUND));});
    }
    public Processing.FailurePlan planFailure(String key,long number,Integer record) {
        authorize(key);if(number<=0 || record!=null && record<0) throw new RemessaFailure(INVALID_REQUEST);
        return transactions.execute(()->{var lot=remessas.inspect(key,number).lote();if(record!=null && record>=lot.records()) throw new RemessaFailure(INVALID_REQUEST);repository.planFailure(lot,repository.generation(scenario),record,clock.instant());return new Processing.FailurePlan(number,record);});
    }
    public void removeFailure(String key,long number) {
        authorize(key);transactions.execute(()->{var lot=remessas.inspect(key,number).lote();repository.removeFailure(lot,repository.generation(scenario),clock.instant());return null;});
    }
    private void authorize(String key) {if(!inspection.inspect(key).scenario().equals(scenario)) throw new RemessaFailure(NOT_FOUND);}
    public synchronized Processing.Configuration configuration(String key) {authorize(key);return transactions.execute(()->repository.configuration(scenario,clock.instant()));}
    public synchronized Processing.Configuration configure(String key,Processing.Mode mode,long latencyMillis) {
        authorize(key);if(mode==null || latencyMillis<0) throw new RemessaFailure(INVALID_REQUEST);
        return transactions.execute(()->{repository.configure(scenario,mode,latencyMillis);return repository.configuration(scenario,clock.instant());});
    }
    public synchronized Processing.Advanced advance(String key,long millis,int steps) {
        authorize(key);if(millis<0 || steps<1) throw new RemessaFailure(INVALID_REQUEST);
        transactions.execute(()->{time.advance(millis);return null;});
        int executed=0;for(int i=0;i<steps;i++) {if(!step()) break;executed++;}
        return new Processing.Advanced(clock.instant(),executed);
    }
    public synchronized void tick() {if(transactions.execute(()->repository.configuration(scenario,clock.instant()).modo())==Processing.Mode.AUTOMATICO) step();}
    private record Candidate(Processing.Work work,Lote lote,boolean begun) {}
    private boolean step() {
        var candidate=transactions.execute(()->{
            var next=repository.next(scenario,clock.instant());if(next.isEmpty()) return null;
            var work=next.get();
            if(work.state()==Processing.State.RECEBIDO) return new Candidate(work,null,repository.begin(work,work.due().plusMillis(work.latencyMillis())));
            var lot=lots.inspectLote(work.loteId(),scenario).orElseThrow(()->new RemessaFailure(NOT_FOUND));
            return new Candidate(work,lot,false);
        });
        if(candidate==null) return false;
        if(candidate.work().state()==Processing.State.RECEBIDO) return candidate.begun();
        if(transactions.execute(()->repository.failurePlan(candidate.work().loteId(),scenario,candidate.work().geracao()).isPresent()))
            return transactions.execute(()->repository.fail(candidate.work(),clock.instant()));
        var records=processor.process(candidate.lote());
        return transactions.execute(()->repository.complete(candidate.work(),records,clock.instant()));
    }
}
