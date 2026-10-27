package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.application.port.in.Processamento;
import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public final class LoteInspectionController {
    private final Processamento processing;
    private final ObjectMapper json;
    public LoteInspectionController(Processamento processing,ObjectMapper json) { this.processing=processing;this.json=json.copy().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS); }
    public record EventResponse(long id,Long numeroLote,String tipo,Instant instante) {}
    public record InspectionResponse(long numeroLote,long identificadorRemessa,String chavePacote,String cenario,String codigoUg,int anoMes,
        int ordem,String assunto,String situacao,int recebidos,int processados,int efetivos,String hash,byte[] bytesBase64,JsonNode registros,JsonNode registrosProcessados,JsonNode registrosEfetivos,List<EventResponse> eventos,boolean ativo,int recebidosHistoricos,JsonNode registrosHistoricos,DiagnosticResponse diagnostico) {}
    @GetMapping("/__simulator/lotes/{numeroLote}")
    public InspectionResponse inspect(@PathVariable String numeroLote,@RequestHeader(value="X-Simulator-Key",required=false) String key) throws java.io.IOException {
        var result=processing.inspect(key,RemessaController.number(numeroLote));var inspection=result.received();var lote=inspection.lote();
        return new InspectionResponse(lote.number(),lote.remessaId(),lote.key(),lote.scenario(),lote.ug(),lote.yearMonth(),lote.order(),
            lote.subject().name(),result.state().name(),result.active()?lote.records():0,result.processed().size(),result.effective().size(),lote.hash(),lote.bytes(),result.active()?json.readTree(lote.bytes()).get(switch(lote.subject()) { case CADASTRO_CONTRIBUINTE -> "cadastrosContribuintes"; case LANCAMENTO_CREDITO_TRIBUTARIO -> "lancamentosCreditosTributarios"; case DIARIO_ARRECADACAO -> "diarioGeralArrecadacao"; case BAIXA_CREDITO_TRIBUTARIO -> "baixasCreditosTributarios"; case SALDO_INICIAL_CREDITO_TRIBUTARIO -> "saldosIniciaisCreditoTributario"; case INICIO_PRAZO_PRESCRICIONAL -> "inicioPrazoPrescricional"; }):json.createArrayNode(),
            records(result.processed()),records(result.effective()),inspection.events().stream().map(e->new EventResponse(e.id(),e.lote(),e.type(),e.instant())).toList(),result.active(),lote.records(),json.readTree(lote.bytes()).elements().next(),result.diagnostic()==null?null:new DiagnosticResponse(result.diagnostic().codigo(),result.diagnostic().mensagem(),result.diagnostic().registro()));
    }
    private JsonNode records(List<byte[]> records) throws java.io.IOException {
        var array=json.createArrayNode();for(byte[] record:records) array.add(json.readTree(record));return array;
    }
}
