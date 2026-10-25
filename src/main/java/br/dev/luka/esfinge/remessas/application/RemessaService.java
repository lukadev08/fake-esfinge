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
        return transactions.execute(() -> {
            var remessa=repository.find(key,session.scenario(),session.ug()).orElseThrow(() -> new RemessaFailure(NOT_FOUND));
            try {
                String hash=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(content.bytes()));
                return repository.receive(remessa,content,hash,clock.instant());
            } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(); }
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
