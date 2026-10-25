package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.autenticacao.application.port.in.Authentication;
import br.dev.luka.esfinge.remessas.application.port.in.Remessas;
import br.dev.luka.esfinge.remessas.domain.RemessaFailure;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class RemessaController {
    private final Remessas remessas;
    private final Authentication authentication;
    private final ContribuintePayload payload=new ContribuintePayload();
    public RemessaController(Remessas remessas, Authentication authentication) { this.remessas=remessas; this.authentication=authentication; }
    public record OpenResponse(String chavePacote) {}
    @GetMapping("/v6/tributario/iniciarEnvio")
    public OpenResponse open(HttpServletRequest request) throws IOException {
        String token=request.getHeader("auth_token"); authentication.authenticate(token);
        if (request.getInputStream().read()!=-1 || !Set.of("anoMes","cancelamentoAutomatico").containsAll(request.getParameterMap().keySet())) throw invalid();
        String month=parameter(request,"anoMes",true), cancellation=parameter(request,"cancelamentoAutomatico",false);
        if (!month.matches("[0-9]+")) throw invalid();
        try { return new OpenResponse(remessas.open(token,Integer.parseInt(month),cancellation).key()); }
        catch (NumberFormatException failure) { throw invalid(); }
    }
    public record ReceiveResponse(String situacao,long numeroLote,java.time.Instant dataEnvio,long identificadorRemessaOnline) {}
    @PostMapping("/v6/tributario/enviarParcialLote")
    public ReceiveResponse receive(HttpServletRequest request) throws IOException {
        String token=request.getHeader("auth_token"); authentication.authenticate(token);
        if (!request.getParameterMap().keySet().equals(Set.of("chavePacote"))) throw invalid();
        try {
            var type=org.springframework.http.MediaType.parseMediaType(request.getContentType());
            if (!type.getType().equalsIgnoreCase("application") || !type.getSubtype().equalsIgnoreCase("json")
                    || (type.getCharset()!=null && !type.getCharset().equals(java.nio.charset.StandardCharsets.UTF_8))) throw invalid();
        } catch (IllegalArgumentException e) { throw invalid(); }
        var content=payload.read(request.getInputStream().readAllBytes());
        var lote=remessas.receive(token,parameter(request,"chavePacote",true),content);
        return new ReceiveResponse("RECEBIDO",lote.number(),lote.receivedAt(),lote.remessaId());
    }
    public record ConsultResponse(String situacao,long identificadorRemessa,long numeroLote,java.time.Instant dataEnvio) {}
    @GetMapping("/v6/tributario/consultarPorNumeroLote/{numeroLote}")
    public ConsultResponse consult(@PathVariable String numeroLote,HttpServletRequest request) throws IOException {
        String token=request.getHeader("auth_token"); authentication.authenticate(token);
        if (request.getInputStream().read()!=-1 || !request.getParameterMap().isEmpty()) throw invalid();
        var lote=remessas.consult(token,number(numeroLote));
        return new ConsultResponse("RECEBIDO",lote.remessaId(),lote.number(),lote.receivedAt());
    }
    static long number(String number) {
        if (!number.matches("-?[0-9]+")) throw invalid();
        try { return Long.parseLong(number); } catch (NumberFormatException e) { throw invalid(); }
    }
    static String parameter(HttpServletRequest request,String name,boolean required) {
        var values=request.getParameterValues(name);
        if (values==null) { if (required) throw invalid(); return null; }
        if (values.length!=1 || (required && values[0].isEmpty())) throw invalid();
        return values[0];
    }
    static RemessaFailure invalid() { return new RemessaFailure(RemessaFailure.Kind.INVALID_REQUEST); }
}
