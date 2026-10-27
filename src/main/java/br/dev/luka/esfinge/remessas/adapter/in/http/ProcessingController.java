package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.application.port.in.Processamento;
import br.dev.luka.esfinge.remessas.domain.Processing;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class ProcessingController {
    private final Processamento processing;
    private final br.dev.luka.esfinge.autenticacao.application.port.in.Authentication authentication;
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    public ProcessingController(Processamento processing,br.dev.luka.esfinge.autenticacao.application.port.in.Authentication authentication) {this.processing=processing;this.authentication=authentication;}
    public record StartResponse(String situacao,java.time.Instant dataEnvio,long identificadorRemessaOnline) {}
    public record ConfigurationResponse(String modo,long latenciaMilissegundos,java.time.Instant instante) {}
    public record FailurePlanResponse(long numeroLote,Integer registro) {}
    public record AdvanceResponse(java.time.Instant instante,int passosExecutados) {}
    @PostMapping("/v6/tributario/iniciar-processamento")
    public StartResponse start(HttpServletRequest request) throws IOException {
        authentication.authenticate(request.getHeader("auth_token"));
        if(request.getInputStream().read()!=-1 || !request.getParameterMap().keySet().equals(Set.of("chavePacote"))) throw RemessaController.invalid();
        var result=processing.start(request.getHeader("auth_token"),RemessaController.parameter(request,"chavePacote",true));
        return new StartResponse(result.situacao(),result.dataEnvio(),result.identificadorRemessaOnline());
    }
    @GetMapping("/__simulator/processamento")
    public ConfigurationResponse configuration(HttpServletRequest request) throws IOException {
        var config=processing.configuration(request.getHeader("X-Simulator-Key"));
        if(!request.getParameterMap().isEmpty() || request.getInputStream().read()!=-1) throw RemessaController.invalid();return response(config);
    }
    @PutMapping("/__simulator/processamento")
    public ConfigurationResponse configure(HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");processing.configuration(key);
        var body=body(request,Set.of("modo","latenciaMilissegundos"));
        if(!body.get("modo").isTextual()) throw RemessaController.invalid();
        Processing.Mode mode;try {mode=Processing.Mode.valueOf(body.get("modo").asText());}catch(IllegalArgumentException failure){throw RemessaController.invalid();}
        return response(processing.configure(key,mode,integer(body.get("latenciaMilissegundos"))));
    }
    @PostMapping("/__simulator/processamento/avancar")
    public AdvanceResponse advance(HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");processing.configuration(key);
        var body=body(request,Set.of("milissegundos","passos"));long steps=integer(body.get("passos"));
        if(steps>Integer.MAX_VALUE || steps<1) throw RemessaController.invalid();
        var result=processing.advance(key,integer(body.get("milissegundos")),(int)steps);
        return new AdvanceResponse(result.instante(),result.passosExecutados());
    }
    @GetMapping("/__simulator/falhas/processamento/{numeroLote}")
    public FailurePlanResponse failurePlan(@PathVariable String numeroLote,HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");processing.configuration(key);empty(request);
        var result=processing.failurePlan(key,positive(numeroLote));
        return new FailurePlanResponse(result.numeroLote(),result.registro());
    }
    @PutMapping("/__simulator/falhas/processamento/{numeroLote}")
    public FailurePlanResponse planFailure(@PathVariable String numeroLote,HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");processing.configuration(key);
        var value=body(request,Set.of("registro"),true).get("registro");Integer record=null;
        if(!value.isNull()) {if(!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue()<0) throw RemessaController.invalid();record=value.intValue();}
        var result=processing.planFailure(key,positive(numeroLote),record);
        return new FailurePlanResponse(result.numeroLote(),result.registro());
    }
    @DeleteMapping("/__simulator/falhas/processamento/{numeroLote}")
    public void removeFailure(@PathVariable String numeroLote,HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");processing.configuration(key);empty(request);processing.removeFailure(key,positive(numeroLote));
    }
    private void empty(HttpServletRequest request) throws IOException {if(request.getQueryString()!=null || !request.getParameterMap().isEmpty() || request.getInputStream().read()!=-1) throw RemessaController.invalid();}
    private long positive(String value) {long number=RemessaController.number(value);if(number<=0) throw RemessaController.invalid();return number;}
    private ConfigurationResponse response(Processing.Configuration config) {return new ConfigurationResponse(config.modo().name(),config.latenciaMilissegundos(),config.instante());}
    private JsonNode body(HttpServletRequest request,Set<String> fields) throws IOException {
        return body(request,fields,false);
    }
    private JsonNode body(HttpServletRequest request,Set<String> fields,boolean nullable) throws IOException {
        if(request.getQueryString()!=null || !request.getParameterMap().isEmpty()) throw RemessaController.invalid();
        try {
            var type=org.springframework.http.MediaType.parseMediaType(request.getContentType());
            if(!type.getType().equalsIgnoreCase("application") || !type.getSubtype().equalsIgnoreCase("json") || (type.getCharset()!=null && !type.getCharset().equals(StandardCharsets.UTF_8))) throw RemessaController.invalid();
            String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(request.getInputStream().readAllBytes())).toString();
            var root=json.readTree(text);if(root==null || !root.isObject() || root.size()!=fields.size()) throw RemessaController.invalid();
            for(String name:fields) if(!root.has(name) || !nullable && !root.hasNonNull(name)) throw RemessaController.invalid();return root;
        } catch(IllegalArgumentException|IOException failure) {throw RemessaController.invalid();}
    }
    private long integer(JsonNode value) {
        try {if(!value.isNumber()) throw RemessaController.invalid();long number=value.decimalValue().longValueExact();if(number<0) throw RemessaController.invalid();return number;}
        catch(ArithmeticException failure) {throw RemessaController.invalid();}
    }
}
