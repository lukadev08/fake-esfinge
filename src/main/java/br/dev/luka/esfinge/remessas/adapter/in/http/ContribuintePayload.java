package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.domain.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import static br.dev.luka.esfinge.remessas.domain.RemessaFailure.Kind.*;

public final class ContribuintePayload {
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final Map<String,Integer> LENGTHS=Map.ofEntries(
        Map.entry("numeroMatriculaContribuinte",16),Map.entry("dataAtualizacaoCadastral",10),Map.entry("tipoContribuinte",2),
        Map.entry("numeroDocumentoContribuinte",14),Map.entry("nomeCompletoContribuinte",512),Map.entry("indicativoCadastroAtivo",1),
        Map.entry("indicativoEspolio",1),Map.entry("numeroMatriculaContribuinteNovo",16),Map.entry("nomeRepresentante",512),
        Map.entry("tipoRepresentante",2),Map.entry("numeroCpfRepresentante",11));
    private static final Set<String> REQUIRED=Set.of("numeroMatriculaContribuinte","dataAtualizacaoCadastral","tipoContribuinte",
            "numeroDocumentoContribuinte","nomeCompletoContribuinte","indicativoCadastroAtivo","indicativoEspolio");
    private static final Set<String> UNSUPPORTED_SUBJECTS=Set.of("lancamentosCreditosTributarios","diarioGeralArrecadacao",
            "baixasCreditosTributarios","saldosIniciaisCreditoTributario","inicioPrazoPrescricional");
    public ReceivedContent read(byte[] bytes) {
        JsonNode root;
        try {
            String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
            root=json.readTree(text);
        } catch (java.io.IOException e) { throw new RemessaFailure(INVALID_REQUEST); }
        if (root==null || root.isMissingNode()) throw new RemessaFailure(INVALID_REQUEST);
        if (!root.isObject() || root.size()!=1) throw invalid();
        String subject=root.fieldNames().next();
        if (UNSUPPORTED_SUBJECTS.contains(subject)) throw new RemessaFailure(UNSUPPORTED);
        if (!subject.equals("cadastrosContribuintes")) throw invalid();
        JsonNode records=root.get(subject);
        if (!records.isArray() || records.isEmpty()) throw invalid();
        for (JsonNode record:records) {
            if (!record.isObject()) throw invalid();
            for (String required:REQUIRED) if (!record.hasNonNull(required)) throw invalid();
            var fields=record.properties().iterator();
            while (fields.hasNext()) {
                var field=fields.next(); var value=field.getValue(); Integer length=LENGTHS.get(field.getKey());
                if (length==null) throw invalid();
                if (value.isNull() && !REQUIRED.contains(field.getKey())) continue;
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>length) throw invalid();
                if (field.getKey().equals("dataAtualizacaoCadastral")) {
                    String date=value.textValue();
                    if (!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw invalid();
                    try { LocalDate.parse(date); } catch (DateTimeParseException e) { throw invalid(); }
                }
            }
        }
        return new ReceivedContent(bytes,records.size());
    }
    private static RemessaFailure invalid() { return new RemessaFailure(INVALID_STRUCTURE); }
}
