package br.dev.luka.esfinge.remessas.adapter.in.http;

import br.dev.luka.esfinge.remessas.domain.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import static br.dev.luka.esfinge.remessas.domain.RemessaFailure.Kind.*;

final class FinalizationPayload {
    private final ObjectMapper json=new ObjectMapper(JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final String[] TOTALS={"quantidadeCadastroContribuinte","quantidadeLancamentoCreditosTributarios","quantidadeDiarioGeralArrecadacao","quantidadeBaixaCreditosTributarios","quantidadeSaldoInicialCreditosTributarios","quantidadeInicioPrazoPrescricional","quantidadeCadastroImobiliario","quantidadeCadastroPropriedadeImobiliaria","quantidadeRevisaoValorLancamentoCreditos","quantidadeEstornoReceitaDiarioGeralArrecadacao","quantidadeCertidaoDividaAtiva","quantidadeCobrancaDividaAtiva","quantidadeSituacaoCobrancaDividaAtiva","quantidadeNovoNumeroInscricaoImobiliaria","quantidadeLancamentoCreditosTributariosOriginal","quantidadeNumeroCadastroImobiliarioLancamento","quantidadeRevisaoTipoCreditoTributario"};
    private static final Assunto[] SUBJECTS={Assunto.CADASTRO_CONTRIBUINTE,Assunto.LANCAMENTO_CREDITO_TRIBUTARIO,Assunto.DIARIO_ARRECADACAO,Assunto.BAIXA_CREDITO_TRIBUTARIO,Assunto.SALDO_INICIAL_CREDITO_TRIBUTARIO,Assunto.INICIO_PRAZO_PRESCRICIONAL};
    record Request(String key,Finalization totals) {}
    Request read(byte[] bytes) {
        JsonNode root;
        try { root=json.readTree(StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()); }
        catch(java.io.IOException failure) {throw new RemessaFailure(INVALID_REQUEST);}
        if(root==null || root.isMissingNode()) throw new RemessaFailure(INVALID_REQUEST);
        if(!root.isObject() || root.size()!=18 || !root.hasNonNull("chavePacote") || !root.get("chavePacote").isTextual() || root.get("chavePacote").textValue().isEmpty()) throw new RemessaFailure(INVALID_STRUCTURE);
        var totals=new EnumMap<Assunto,BigInteger>(Assunto.class);
        for(int i=0;i<TOTALS.length;i++) {
            var value=root.get(TOTALS[i]);
            if(value==null || !value.isTextual() || !value.textValue().matches("[0-9]+")) throw new RemessaFailure(INVALID_STRUCTURE);
            var count=new BigInteger(value.textValue());
            if(i<6) totals.put(SUBJECTS[i],count);
            else if(!value.textValue().equals("0")) throw new RemessaFailure(count.signum()>0?UNSUPPORTED:INVALID_STRUCTURE);
        }
        return new Request(root.get("chavePacote").textValue(),new Finalization(totals));
    }
}
