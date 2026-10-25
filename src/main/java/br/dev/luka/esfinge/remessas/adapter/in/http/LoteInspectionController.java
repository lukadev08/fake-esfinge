package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.application.port.in.Remessas;
import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public final class LoteInspectionController {
    private final Remessas remessas;
    private final ObjectMapper json;
    public LoteInspectionController(Remessas remessas,ObjectMapper json) { this.remessas=remessas;this.json=json.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS); }
    public record EventResponse(long id,Long numeroLote,String tipo,Instant instante) {}
    public record InspectionResponse(long numeroLote,long identificadorRemessa,String chavePacote,String cenario,String codigoUg,int anoMes,
        int ordem,String assunto,String situacao,int recebidos,int processados,int efetivos,String hash,byte[] bytesBase64,JsonNode registros,List<EventResponse> eventos) {}
    @GetMapping("/__simulator/lotes/{numeroLote}")
    public InspectionResponse inspect(@PathVariable String numeroLote,@RequestHeader(value="X-Simulator-Key",required=false) String key) throws java.io.IOException {
        var inspection=remessas.inspect(key,RemessaController.number(numeroLote));var lote=inspection.lote();
        return new InspectionResponse(lote.number(),lote.remessaId(),lote.key(),lote.scenario(),lote.ug(),lote.yearMonth(),lote.order(),
            lote.subject().name(),"RECEBIDO",lote.records(),0,0,lote.hash(),lote.bytes(),json.readTree(lote.bytes()).get(switch(lote.subject()) { case CADASTRO_CONTRIBUINTE -> "cadastrosContribuintes"; case LANCAMENTO_CREDITO_TRIBUTARIO -> "lancamentosCreditosTributarios"; case DIARIO_ARRECADACAO -> "diarioGeralArrecadacao"; case BAIXA_CREDITO_TRIBUTARIO -> "baixasCreditosTributarios"; case SALDO_INICIAL_CREDITO_TRIBUTARIO -> "saldosIniciaisCreditoTributario"; case INICIO_PRAZO_PRESCRICIONAL -> "inicioPrazoPrescricional"; }),
            inspection.events().stream().map(e->new EventResponse(e.id(),e.lote(),e.type(),e.instant())).toList());
    }
}
