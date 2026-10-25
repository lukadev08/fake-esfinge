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

public final class LotePayload {
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    private static final Map<String,Integer> LENGTHS=Map.ofEntries(
        Map.entry("numeroMatriculaContribuinte",16),Map.entry("dataAtualizacaoCadastral",10),Map.entry("tipoContribuinte",2),
        Map.entry("numeroDocumentoContribuinte",14),Map.entry("nomeCompletoContribuinte",512),Map.entry("indicativoCadastroAtivo",1),
        Map.entry("indicativoEspolio",1),Map.entry("numeroMatriculaContribuinteNovo",16),Map.entry("nomeRepresentante",512),
        Map.entry("tipoRepresentante",2),Map.entry("numeroCpfRepresentante",11));
    private static final Set<String> REQUIRED=Set.of("numeroMatriculaContribuinte","dataAtualizacaoCadastral","tipoContribuinte",
            "numeroDocumentoContribuinte","nomeCompletoContribuinte","indicativoCadastroAtivo","indicativoEspolio");
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
        if (!subject.equals("cadastrosContribuintes") && !subject.equals("lancamentosCreditosTributarios") && !subject.equals("diarioGeralArrecadacao") && !subject.equals("baixasCreditosTributarios") && !subject.equals("saldosIniciaisCreditoTributario") && !subject.equals("inicioPrazoPrescricional")) throw invalid();
        JsonNode records=root.get(subject);
        if (!records.isArray() || records.isEmpty()) throw invalid();
        if (subject.equals("inicioPrazoPrescricional")) {
            for (JsonNode record:records) prescription(record);
            return new ReceivedContent(bytes,records.size(),Assunto.INICIO_PRAZO_PRESCRICIONAL,List.of());
        }
        if (!subject.equals("cadastrosContribuintes")) {
            var values=new ArrayList<Map<String,java.math.BigDecimal>>();
            for (JsonNode record:records) values.add(switch(subject) { case "lancamentosCreditosTributarios" -> Map.of("valorLancamentoCreditoTributario",credit(record)); case "diarioGeralArrecadacao" -> Map.of("valorCreditoTributario",collection(record)); case "baixasCreditosTributarios" -> Map.of("valorOperacaoBaixa",writeOff(record)); default -> initialBalance(record); });
            return new ReceivedContent(bytes,records.size(),switch(subject) { case "lancamentosCreditosTributarios" -> Assunto.LANCAMENTO_CREDITO_TRIBUTARIO; case "diarioGeralArrecadacao" -> Assunto.DIARIO_ARRECADACAO; case "baixasCreditosTributarios" -> Assunto.BAIXA_CREDITO_TRIBUTARIO; default -> Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO; },values);
        }
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
        return new ReceivedContent(bytes,records.size(),Assunto.CADASTRO_CONTRIBUINTE,List.of());
    }
    private static final Set<String> CREDIT_REQUIRED=Set.of("anoLancamentoCreditoTributario","codigoLancamentoCreditoTributario","tipoOperacao","tipoCredito",
            "numeroMatriculaContribuinte","anoMesCompetencia","dataLancamento","dataVencimento","valorLancamentoCreditoTributario","numeroCpfResponsavelOperacao","codigoEspecificacaoNaturezaReceita");
    private static final Set<String> INTEGERS=Set.of("anoLancamentoCreditoTributario","tipoOperacao","tipoCredito","anoMesCompetencia","codigoEspecificacaoNaturezaReceita");
    private static final Map<String,Integer> CREDIT_STRINGS=Map.of("codigoLancamentoCreditoTributario",16,"numeroMatriculaContribuinte",16,
            "numeroCpfResponsavelOperacao",11,"observacaoLancamentoCreditoTributario",512,"detalhamentoTipoCredito",50);
    private static final Set<String> DATES=Set.of("dataLancamento","dataVencimento","dataImportacaoSimplesNacional");
    private static java.math.BigDecimal credit(JsonNode record) {
        if (!record.isObject()) throw invalid();
        for (String name:CREDIT_REQUIRED) if (!record.hasNonNull(name)) throw invalid();
        for (var field:record.properties()) {
            String name=field.getKey();JsonNode value=field.getValue();
            if (!CREDIT_REQUIRED.contains(name) && !CREDIT_STRINGS.containsKey(name) && !DATES.contains(name)) throw invalid();
            if (value.isNull() && !CREDIT_REQUIRED.contains(name)) continue;
            if (INTEGERS.contains(name)) {
                if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>0) throw invalid();
                if (name.equals("anoMesCompetencia")) {
                    var integer=value.decimalValue();
                    if (integer.scale()>0) integer=integer.stripTrailingZeros();
                    if (integer.scale() < -1) throw invalid();
                    var month=integer.unscaledValue().remainder(java.math.BigInteger.valueOf(100));
                    if (integer.scale()==-1) month=month.multiply(java.math.BigInteger.TEN).remainder(java.math.BigInteger.valueOf(100));
                    if (month.signum()<1 || month.compareTo(java.math.BigInteger.valueOf(12))>0) throw invalid();
                }
            } else if (DATES.contains(name)) {
                if (!value.isTextual() || !value.textValue().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw invalid();
                try { LocalDate.parse(value.textValue()); } catch (DateTimeParseException e) { throw invalid(); }
            } else if (CREDIT_STRINGS.containsKey(name)) {
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>CREDIT_STRINGS.get(name)) throw invalid();
            } else if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>2) throw invalid();
        }
        return record.get("valorLancamentoCreditoTributario").decimalValue();
    }
    private static final Set<String> COLLECTION_REQUIRED=Set.of("anoCodigoSequencialOperacaoArrecadacao","codigoSequencialOperacaoArrecadacao","tipoArrecadacao","dataOperacao",
            "codigoEspecificacaoReceita","anoLancamentoCreditoTributario","codigoLancamentoCreditoTributario","valorCreditoTributario");
    private static final Set<String> COLLECTION_INTEGERS=Set.of("anoCodigoSequencialOperacaoArrecadacao","tipoArrecadacao","codigoEspecificacaoReceita","anoLancamentoCreditoTributario","numeroParcelas");
    private static final Map<String,Integer> COLLECTION_STRINGS=Map.of("codigoSequencialOperacaoArrecadacao",16,"codigoLancamentoCreditoTributario",16,"numeroAnoTipoCobranca",50);
    private static java.math.BigDecimal collection(JsonNode record) {
        if (!record.isObject()) throw invalid();
        for (String name:COLLECTION_REQUIRED) if (!record.hasNonNull(name)) throw invalid();
        for (var field:record.properties()) {
            String name=field.getKey();JsonNode value=field.getValue();
            if (!COLLECTION_REQUIRED.contains(name) && !COLLECTION_STRINGS.containsKey(name) && !COLLECTION_INTEGERS.contains(name)) throw invalid();
            if (value.isNull() && !COLLECTION_REQUIRED.contains(name)) continue;
            if (COLLECTION_INTEGERS.contains(name)) {
                if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>0) throw invalid();
            } else if (name.equals("dataOperacao")) {
                if (!value.isTextual() || !value.textValue().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw invalid();
                try { LocalDate.parse(value.textValue()); } catch (DateTimeParseException e) { throw invalid(); }
            } else if (COLLECTION_STRINGS.containsKey(name)) {
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>COLLECTION_STRINGS.get(name)) throw invalid();
            } else if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>2) throw invalid();
        }
        return record.get("valorCreditoTributario").decimalValue();
    }
    private static final Set<String> WRITE_OFF_REQUIRED=Set.of("anoLancamentoCreditoTributario","codigoLancamentoCreditoTributario","codigoSequencialOperacaoBaixa","tipoBaixa","dataBaixa","valorOperacaoBaixa","indicativoEstornoBaixa","motivoBaixa","numeroCpfResponsavelOperacao");
    private static final Set<String> WRITE_OFF_INTEGERS=Set.of("anoLancamentoCreditoTributario","codigoSequencialOperacaoBaixa","tipoBaixa","anoLancamentoCreditoTributarioDividaAtiva");
    private static final Map<String,Integer> WRITE_OFF_STRINGS=Map.of("codigoLancamentoCreditoTributario",16,"indicativoEstornoBaixa",1,"motivoBaixa",512,"codigoLancamentoCreditoTributarioDividaAtiva",16,"numeroCpfResponsavelOperacao",11);
    private static java.math.BigDecimal writeOff(JsonNode record) {
        if (!record.isObject()) throw invalid();
        for (String name:WRITE_OFF_REQUIRED) if (!record.hasNonNull(name)) throw invalid();
        for (var field:record.properties()) {
            String name=field.getKey();JsonNode value=field.getValue();
            if (!WRITE_OFF_REQUIRED.contains(name) && !WRITE_OFF_STRINGS.containsKey(name) && !WRITE_OFF_INTEGERS.contains(name)) throw invalid();
            if (value.isNull() && !WRITE_OFF_REQUIRED.contains(name)) continue;
            if (WRITE_OFF_INTEGERS.contains(name)) {
                if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>0) throw invalid();
            } else if (name.equals("dataBaixa")) {
                if (!value.isTextual() || !value.textValue().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw invalid();
                try { LocalDate.parse(value.textValue()); } catch (DateTimeParseException e) { throw invalid(); }
            } else if (WRITE_OFF_STRINGS.containsKey(name)) {
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>WRITE_OFF_STRINGS.get(name)) throw invalid();
            } else if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>2) throw invalid();
        }
        return record.get("valorOperacaoBaixa").decimalValue();
    }
    private static final Set<String> BALANCE_MONEY=Set.of("saldoPrincipal","saldoMulta","saldoJuros","saldoAtualizacaoMonetaria");
    private static final Set<String> BALANCE_REQUIRED=Set.of("anoLancamentoCreditoTributario","codigoLancamentoCreditoTributario","saldoPrincipal","saldoMulta","saldoJuros","saldoAtualizacaoMonetaria");
    private static Map<String,java.math.BigDecimal> initialBalance(JsonNode record) {
        if (!record.isObject()) throw invalid();
        for (String name:BALANCE_REQUIRED) if (!record.hasNonNull(name)) throw invalid();
        var values=new HashMap<String,java.math.BigDecimal>();
        for (var field:record.properties()) {
            String name=field.getKey();JsonNode value=field.getValue();
            if (!BALANCE_REQUIRED.contains(name) && !name.equals("tipoSaldoInicial")) throw invalid();
            if (value.isNull() && name.equals("tipoSaldoInicial")) continue;
            if (BALANCE_MONEY.contains(name)) {
                if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>2) throw invalid();
                values.put(name,value.decimalValue());
            } else if (name.equals("codigoLancamentoCreditoTributario")) {
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>16) throw invalid();
            } else if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>0) throw invalid();
        }
        return values;
    }
    private static final String PRESCRIPTION_YEAR="anoLancamentoCreditoTributarioOuDividaAtiva",PRESCRIPTION_CODE="codigoLancamentoCreditoTributarioOuNumeroInscricaoDividaAtiva";
    private static final Set<String> PRESCRIPTION_REQUIRED=Set.of(PRESCRIPTION_YEAR,PRESCRIPTION_CODE,"dataInicioPrazoPrescricional","dataAtualizacaoPrazoPrescricional");
    private static void prescription(JsonNode record) {
        if (!record.isObject()) throw invalid();
        for (String name:PRESCRIPTION_REQUIRED) if (!record.hasNonNull(name)) throw invalid();
        for (var field:record.properties()) {
            String name=field.getKey();JsonNode value=field.getValue();
            if (!PRESCRIPTION_REQUIRED.contains(name)) throw invalid();
            if (name.equals(PRESCRIPTION_YEAR)) {
                if (!value.isNumber() || value.decimalValue().stripTrailingZeros().scale()>0) throw invalid();
            } else if (name.equals(PRESCRIPTION_CODE)) {
                if (!value.isTextual() || value.textValue().codePointCount(0,value.textValue().length())>16) throw invalid();
            } else {
                if (!value.isTextual() || !value.textValue().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw invalid();
                try { LocalDate.parse(value.textValue()); } catch (DateTimeParseException e) { throw invalid(); }
            }
        }
    }
    private static RemessaFailure invalid() { return new RemessaFailure(INVALID_STRUCTURE); }
}
