package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.cenarios.application.port.in.InspectScenario;
import br.dev.luka.esfinge.remessas.application.port.in.Remessas;
import br.dev.luka.esfinge.remessas.domain.Competencia;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import org.springframework.web.bind.annotation.*;

@RestController
public final class CompetenciaController {
    private final Remessas remessas;
    private final InspectScenario inspection;
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build()).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    public CompetenciaController(Remessas remessas,InspectScenario inspection) {this.remessas=remessas;this.inspection=inspection;}
    public record CompetenciaResponse(String ug,int anoMes,boolean bloqueada) {}
    @GetMapping("/__simulator/competencias/{ug}/{anoMes}")
    public CompetenciaResponse get(@PathVariable String ug,@PathVariable String anoMes,HttpServletRequest request) throws IOException {
        int month=target(anoMes,request);if(request.getInputStream().read()!=-1) throw RemessaController.invalid();
        return response(remessas.competencia(request.getHeader("X-Simulator-Key"),ug,month));
    }
    @PutMapping("/__simulator/competencias/{ug}/{anoMes}")
    public CompetenciaResponse put(@PathVariable String ug,@PathVariable String anoMes,HttpServletRequest request) throws IOException {
        int month=target(anoMes,request);
        try {
            var media=org.springframework.http.MediaType.parseMediaType(request.getContentType());
            if(!media.getType().equalsIgnoreCase("application") || !media.getSubtype().equalsIgnoreCase("json") || media.getCharset()!=null && !media.getCharset().equals(StandardCharsets.UTF_8)) throw RemessaController.invalid();
            String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(request.getInputStream().readAllBytes())).toString();
            var root=json.readTree(text);
            if(root==null || !root.isObject() || root.size()!=1 || !root.has("bloqueada") || !root.get("bloqueada").isBoolean()) throw RemessaController.invalid();
            return response(remessas.blockCompetencia(request.getHeader("X-Simulator-Key"),ug,month,root.get("bloqueada").booleanValue()));
        } catch(IllegalArgumentException|IOException failure) {throw RemessaController.invalid();}
    }
    private int target(String month,HttpServletRequest request) {
        inspection.inspect(request.getHeader("X-Simulator-Key"));
        if(request.getQueryString()!=null || !request.getParameterMap().isEmpty() || !month.matches("2026(0[1-9]|1[0-2])")) throw RemessaController.invalid();
        return Integer.parseInt(month);
    }
    private CompetenciaResponse response(Competencia value) {return new CompetenciaResponse(value.ug(),value.anoMes(),value.bloqueada());}
}
