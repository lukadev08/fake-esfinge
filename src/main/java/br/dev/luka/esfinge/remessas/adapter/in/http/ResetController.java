package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.remessas.application.port.in.Processamento;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class ResetController {
    private final Processamento processing;
    private final InspectScenario inspection;
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build()).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    public ResetController(Processamento processing,InspectScenario inspection) {this.processing=processing;this.inspection=inspection;}
    public record ResetResponse(String cenario,long geracaoAnterior,long geracao,long remessasRemovidas,long lotesRemovidos,long sessoesInvalidadas) {}
    @PostMapping("/__simulator/reset")
    public ResetResponse reset(HttpServletRequest request) throws IOException {
        String key=request.getHeader("X-Simulator-Key");inspection.inspect(key);
        if(request.getQueryString()!=null || !request.getParameterMap().isEmpty()) throw RemessaController.invalid();
        try {
            var media=org.springframework.http.MediaType.parseMediaType(request.getContentType());
            if(!media.getType().equalsIgnoreCase("application") || !media.getSubtype().equalsIgnoreCase("json") || media.getCharset()!=null && !media.getCharset().equals(StandardCharsets.UTF_8)) throw RemessaController.invalid();
            String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(request.getInputStream().readAllBytes())).toString();
            var root=json.readTree(text);
            if(root==null || !root.isObject() || root.size()!=2 || !root.has("cenario") || !root.get("cenario").isTextual() || root.get("cenario").textValue().isEmpty()
                || !root.has("geracaoEsperada") || !root.get("geracaoEsperada").isIntegralNumber() || !root.get("geracaoEsperada").canConvertToLong() || root.get("geracaoEsperada").longValue()<0) throw RemessaController.invalid();
            var result=processing.reset(key,root.get("cenario").textValue(),root.get("geracaoEsperada").longValue());
            return new ResetResponse(result.cenario(),result.geracaoAnterior(),result.geracao(),result.remessasRemovidas(),result.lotesRemovidos(),result.sessoesInvalidadas());
        } catch(IllegalArgumentException|IOException failure) {throw RemessaController.invalid();}
    }
}
