package br.dev.luka.esfinge.remessas.application;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.remessas.application.port.in.Remessas;
import br.dev.luka.esfinge.remessas.application.port.out.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Clock;
import java.util.UUID;
import static br.dev.luka.esfinge.remessas.domain.RemessaFailure.Kind.*;

public final class RemessaService implements Remessas {
    private final Authentication authentication;
    private final RemessaRepository repository;
    private final Transactions transactions;
    private final Clock clock;
    private final br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario inspection;
    public RemessaService(Authentication authentication, RemessaRepository repository, Transactions transactions, Clock clock, br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario inspection) {
        this.authentication = authentication; this.repository = repository; this.transactions = transactions; this.clock = clock; this.inspection=inspection;
    }
    public Remessa open(String token, int yearMonth, String cancellation) {
        var session = authentication.authenticate(token);
        if (yearMonth < 202601 || yearMonth > 202612) throw new RemessaFailure(INVALID_REQUEST);
        if (!"NAO".equals(cancellation)) throw new RemessaFailure(UNSUPPORTED);
        return transactions.execute(() -> repository.open(session.scenario(), session.ug(), yearMonth, "sim-envio-" + UUID.randomUUID(), clock.instant()));
    }
    public Lote receive(String token,String key,ReceivedContent content) {
        var session=authentication.authenticate(token);
        if (key==null || key.isEmpty()) throw new RemessaFailure(INVALID_REQUEST);
        if (content==null || content.records()<1 || content.bytes().length==0) throw new RemessaFailure(INVALID_STRUCTURE);
        var result=transactions.execute(() -> {
            var remessa=repository.find(key,session.scenario(),session.ug()).orElseThrow(() -> new RemessaFailure(NOT_FOUND));
            remessa.requireOpen();
            var interruption=repository.receptionAttempt(remessa,content.subject());
            if(interruption==Reception.Type.ANTES_ACEITACAO) return new Receipt(null,interruption);
            try {
                String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(content.bytes()));
                var now=clock.instant();var lot=repository.receive(remessa,content,hash,now);
                if(interruption==Reception.Type.APOS_COMMIT) repository.responseLost(remessa,lot,now);
                return new Receipt(lot,interruption);
            } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(); }
        });
        if(result.interruption()!=null) throw new Reception.Interrupted(result.interruption());
        return result.lot();
    }
    private record Receipt(Lote lot,Reception.Type interruption) {}
    public Reception.Plan receptionPlan(String adminKey,String key,Assunto subject) {
        var scenario=inspection.inspect(adminKey).scenario();validatePlanKey(key,subject);
        return transactions.execute(()->repository.receptionPlan(repository.find(key,scenario).orElseThrow(()->new RemessaFailure(NOT_FOUND)),subject).orElseThrow(()->new RemessaFailure(NOT_FOUND)));
    }
    public Reception.Plan planReception(String adminKey,String key,Assunto subject,Reception.Type type,long attempt) {
        var scenario=inspection.inspect(adminKey).scenario();validatePlanKey(key,subject);
        if(type==null || attempt<1) throw new RemessaFailure(INVALID_REQUEST);
        return transactions.execute(()->{
            var remessa=repository.find(key,scenario).orElseThrow(()->new RemessaFailure(NOT_FOUND));remessa.requireOpen();
            repository.planReception(remessa,subject,type,attempt,clock.instant());return repository.receptionPlan(remessa,subject).orElseThrow();
        });
    }
    public void removeReceptionPlan(String adminKey,String key,Assunto subject) {
        var scenario=inspection.inspect(adminKey).scenario();validatePlanKey(key,subject);
        transactions.execute(()->{var remessa=repository.find(key,scenario).orElseThrow(()->new RemessaFailure(NOT_FOUND));remessa.requireOpen();repository.removeReceptionPlan(remessa,subject,clock.instant());return null;});
    }
    private void validatePlanKey(String key,Assunto subject) {if(key==null || key.isEmpty() || subject==null) throw new RemessaFailure(INVALID_REQUEST);}
    public void finish(String token,String key,Finalization totals) {
        var session=authentication.authenticate(token);
        if(key==null || key.isEmpty()) throw new RemessaFailure(INVALID_STRUCTURE);
        if(totals==null) throw new RemessaFailure(INVALID_STRUCTURE);
        transactions.execute(()->{
            var remessa=repository.find(key,session.scenario(),session.ug()).orElseThrow(()->new RemessaFailure(NOT_FOUND));
            remessa.requireOpen();totals.verify(repository.finalization(remessa));repository.finish(remessa,clock.instant());return null;
        });
    }
    public Lote consult(String token,long number) {
        var session=authentication.authenticate(token);
        return transactions.execute(() -> {
            var lote=repository.findLote(number,session.scenario(),session.ug()).orElseThrow(() -> new RemessaFailure(NOT_FOUND));
            repository.touch(lote,clock.instant());
            return lote;
        });
    }
    public LoteInspection inspect(String adminKey,long number) {
        var scenario=inspection.inspect(adminKey).scenario();
        return transactions.execute(() -> {
            var lote=repository.inspectLote(number,scenario).orElseThrow(() -> new RemessaFailure(NOT_FOUND));
            return new LoteInspection(lote,repository.events(lote.remessaId()));
        });
    }
}
