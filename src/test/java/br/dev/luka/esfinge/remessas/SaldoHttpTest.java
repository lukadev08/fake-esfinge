package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class,properties="simulator.users[0].ugs=0001,0002")
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
class SaldoHttpTest {
    static final String RECORD="{\"anoLancamentoCreditoTributario\":2015,\"codigoLancamentoCreditoTributario\":\"000007\",\"saldoPrincipal\":123456789012345678901234567890.12,\"saldoMulta\":2.30,\"saldoJuros\":4.56,\"saldoAtualizacaoMonetaria\":7.89}";
    static final String PAYLOAD="{\"saldosIniciaisCreditoTributario\":["+RECORD+"]}";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-saldo-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
    }
    String token(String ug) throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
            .param("codigoUg",ug).param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token,int month) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",Integer.toString(month))
            .param("cancelamentoAutomatico","NAO")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    ResultActions receive(String token,String key,String body) throws Exception {
        return http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(body));
    }
    long number(ResultActions response) throws Exception {return json.readTree(response.andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();}
    @Test void janeiroRecebeConsultaEInspecionaQuatroValoresExatos() throws Exception {
        String token=token("0001"),key=open(token,202601);long number=number(receive(token,key,PAYLOAD));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        String body=http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.assunto").value("SALDO_INICIAL_CREDITO_TRIBUTARIO")).andExpect(jsonPath("$.anoMes").value(202601))
            .andExpect(jsonPath("$.recebidos").value(1)).andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0))
            .andReturn().getResponse().getContentAsString();
        var exact=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(body);var record=exact.get("registros").get(0);
        for(var field:java.util.Map.of("saldoPrincipal","123456789012345678901234567890.12","saldoMulta","2.30","saldoJuros","4.56","saldoAtualizacaoMonetaria","7.89").entrySet())
            assertEquals(0,new BigDecimal(field.getValue()).compareTo(record.get(field.getKey()).decimalValue()));
        assertFalse(record.has("tipoSaldoInicial"));assertEquals("000007",record.get("codigoLancamentoCreditoTributario").asText());
        assertArrayEquals(PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),exact.get("bytesBase64").binaryValue());
    }
    com.fasterxml.jackson.databind.node.ObjectNode root() throws Exception {
        return (com.fasterxml.jackson.databind.node.ObjectNode)new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(PAYLOAD);
    }
    com.fasterxml.jackson.databind.node.ObjectNode record(com.fasterxml.jackson.databind.node.ObjectNode root) {
        return (com.fasterxml.jackson.databind.node.ObjectNode)root.get("saldosIniciaisCreditoTributario").get(0);
    }
    @Test void schemaObrigatoriosOpcionaisTiposELimitesSemDefaults() throws Exception {
        String token=token("0001"),key=open(token,202602);
        var schema=json.readTree(Files.readString(Path.of("docs/contratos/2026-v6-6.06/schemas.json"))).get("$defs").get("saldo");
        var required=new java.util.HashSet<String>();schema.get("required").forEach(n->required.add(n.asText()));
        for(String name:required) {
            var root=root();record(root).remove(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            record(root).putNull(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        }
        for(var field:schema.get("properties").properties()) {
            String name=field.getKey();var root=root();var record=record(root);
            for(String invalid:name.equals("codigoLancamentoCreditoTributario")?new String[]{"1","true","[]","{}"}:new String[]{"\"1\"","true","[]","{}"}) {
                record.set(name,json.readTree(invalid));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
        }
        var root=root();record(root).put("codigoLancamentoCreditoTributario","😀".repeat(16));number(receive(token,key,root.toString()));
        record(root).put("codigoLancamentoCreditoTributario","😀".repeat(17));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        record(root).put("codigoLancamentoCreditoTributario","");number(receive(token,key,root.toString()));
    }
    @Test void cadaValorMonetarioEInteirosSaoExatosSemCoercao() throws Exception {
        String token=token("0001"),key=open(token,202603);
        for(String name:java.util.List.of("saldoPrincipal","saldoMulta","saldoJuros","saldoAtualizacaoMonetaria")) {
            for(String value:new String[]{"1","1.0","1.00","0","-1.20","1e2","100.000"}) {
                var root=root();record(root).set(name,json.readTree(value));number(receive(token,key,root.toString()));
            }
            for(String value:new String[]{"1.001","1.005","0.001","0.01000000000000000000000000000000000001"}) {
                var root=root();record(root).set(name,new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(value));
                receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
        }
        for(String name:java.util.List.of("anoLancamentoCreditoTributario","tipoSaldoInicial")) {
            var root=root();record(root).put(name,new java.math.BigInteger("123456789012345678901234567890"));number(receive(token,key,root.toString()));
            record(root).put(name,new BigDecimal("1.0"));number(receive(token,key,root.toString()));
            record(root).put(name,new BigDecimal("1.5"));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            record(root).put(name,-1);number(receive(token,key,root.toString()));
        }
    }
    @Test void extrasMisturasEDocumentosInvalidosNaoGeramEfeitos() throws Exception {
        String token=token("0001"),key=open(token,202604);
        var extra=root();record(extra).put("extra","ficticio");receive(token,key,extra.toString()).andExpect(status().isUnprocessableEntity());
        for(String body:new String[]{"{\"saldosIniciaisCreditoTributario\":[]}","{\"saldosIniciaisCreditoTributario\":null}","{\"saldosIniciaisCreditoTributario\":[null]}",
                PAYLOAD.substring(0,PAYLOAD.length()-1)+",\"cadastrosContribuintes\":[]}",PAYLOAD.substring(0,PAYLOAD.length()-1)+",\"baixasCreditosTributarios\":[]}"})
            receive(token,key,body).andExpect(status().isUnprocessableEntity());
        for(String body:new String[]{PAYLOAD+PAYLOAD,PAYLOAD.replace("\"saldoMulta\":2.30","\"saldoMulta\":2.30,\"saldoMulta\":9")})
            receive(token,key,body).andExpect(status().isBadRequest());
        long number=number(receive(token,key,PAYLOAD));
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.eventos.length()").value(2));
    }
    @Test void variosLotesPreservamMultiplicidadeEIsolamento() throws Exception {
        String token=token("0001"),key=open(token,202605),other=token("0002");
        long first=number(receive(token,key,PAYLOAD)),second=number(receive(token,key,PAYLOAD));assertNotEquals(first,second);
        var root=root();root.withArray("saldosIniciaisCreditoTributario").add(record(root).deepCopy());long duplicate=number(receive(token,key,root.toString()));
        http.perform(get("/__simulator/lotes/"+duplicate).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.recebidos").value(2)).andExpect(jsonPath("$.registros.length()").value(2)).andExpect(jsonPath("$.ordem").value(3));
        number(receive(token,key,RemessaHttpTest.PAYLOAD));number(receive(token,key,CreditoHttpTest.PAYLOAD));
        number(receive(token,key,ArrecadacaoHttpTest.PAYLOAD));number(receive(token,key,BaixaHttpTest.PAYLOAD));
        receive(other,key,PAYLOAD).andExpect(status().isNotFound());receive("invalido",key,PAYLOAD).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+first).header("auth_token",other)).andExpect(status().isNotFound());
        http.perform(get("/__simulator/lotes/"+first)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+first).header("X-Simulator-Key",token)).andExpect(status().isUnauthorized());
    }
    @Test void tipoOpcionalNaoRecebeDefaultNemRegraSomenteJaneiro() throws Exception {
        String token=token("0001"),key=open(token,202606);long absent=number(receive(token,key,PAYLOAD));
        var root=root();record(root).putNull("tipoSaldoInicial");long nullable=number(receive(token,key,root.toString()));
        for(long number:new long[]{absent,nullable}) {
            var body=json.readTree(http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.efetivos").value(0)).andReturn().getResponse().getContentAsString());
            if(number==absent) assertFalse(body.get("registros").get(0).has("tipoSaldoInicial"));
            else assertTrue(body.get("registros").get(0).get("tipoSaldoInicial").isNull());
        }
    }
}
