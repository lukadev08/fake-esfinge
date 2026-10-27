package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.remessas.application.port.in.Remessas;
import br.dev.luka.esfinge.remessas.domain.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import java.util.Set;
import org.springframework.web.bind.annotation.*;

@RestController
public final class ReceptionFailureController {
    private final Remessas remessas;
    private final InspectScenario inspection;
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build()).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    public ReceptionFailureController(Remessas remessas,InspectScenario inspection) {this.remessas=remessas;this.inspection=inspection;}
    public record PlanResponse(String chavePacote,String assunto,String tipo,long tentativa,long tentativas,boolean consumida,boolean recuperacaoPendente) {}
    @GetMapping("/__simulator/falhas/recepcao")
    public PlanResponse get(HttpServletRequest request) throws IOException {
        var target=target(request);if(request.getInputStream().read()!=-1) throw RemessaController.invalid();
        return response(remessas.receptionPlan(request.getHeader("X-Simulator-Key"),target.key(),target.subject()));
    }
    @PutMapping("/__simulator/falhas/recepcao")
    public PlanResponse put(HttpServletRequest request) throws IOException {
        var target=target(request);
        try {
            var media=org.springframework.http.MediaType.parseMediaType(request.getContentType());
            if(!media.getType().equalsIgnoreCase("application") || !media.getSubtype().equalsIgnoreCase("json") || media.getCharset()!=null && !media.getCharset().equals(StandardCharsets.UTF_8)) throw RemessaController.invalid();
            String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(request.getInputStream().readAllBytes())).toString();
            var root=json.readTree(text);
            if(root==null || !root.isObject() || root.size()!=2 || !root.hasNonNull("tipo") || !root.get("tipo").isTextual() || !root.hasNonNull("tentativa") || !root.get("tentativa").isIntegralNumber() || !root.get("tentativa").canConvertToLong() || root.get("tentativa").longValue()<1) throw RemessaController.invalid();
            return response(remessas.planReception(request.getHeader("X-Simulator-Key"),target.key(),target.subject(),Reception.Type.valueOf(root.get("tipo").textValue()),root.get("tentativa").longValue()));
        } catch(IllegalArgumentException|IOException failure) {throw RemessaController.invalid();}
    }
    @DeleteMapping("/__simulator/falhas/recepcao")
    public void delete(HttpServletRequest request) throws IOException {
        var target=target(request);if(request.getInputStream().read()!=-1) throw RemessaController.invalid();
        remessas.removeReceptionPlan(request.getHeader("X-Simulator-Key"),target.key(),target.subject());
    }
    private record Target(String key,Assunto subject) {}
    private Target target(HttpServletRequest request) {
        inspection.inspect(request.getHeader("X-Simulator-Key"));
        if(!request.getParameterMap().keySet().equals(Set.of("chavePacote","assunto"))) throw RemessaController.invalid();
        String key=RemessaController.parameter(request,"chavePacote",true),subject=RemessaController.parameter(request,"assunto",true);
        try {return new Target(key,Assunto.valueOf(subject));}catch(IllegalArgumentException failure){throw new RemessaFailure(RemessaFailure.Kind.UNSUPPORTED);}
    }
    private PlanResponse response(Reception.Plan plan) {return new PlanResponse(plan.chavePacote(),plan.assunto().name(),plan.tipo().name(),plan.tentativa(),plan.tentativas(),plan.consumida(),plan.recuperacaoPendente());}
}
