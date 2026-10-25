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
class CreditoHttpTest {
    static final String RECORD="{\"anoLancamentoCreditoTributario\":2020,\"codigoLancamentoCreditoTributario\":\"000009\",\"tipoOperacao\":1,\"tipoCredito\":1,\"numeroMatriculaContribuinte\":\"0007\",\"anoMesCompetencia\":202002,\"dataLancamento\":\"2020-02-29\",\"dataVencimento\":\"2020-03-01\",\"valorLancamentoCreditoTributario\":123456789012345678901234567890.12,\"numeroCpfResponsavelOperacao\":\"00000000001\",\"codigoEspecificacaoNaturezaReceita\":1}";
    static final String PAYLOAD="{\"lancamentosCreditosTributarios\":["+RECORD+"]}";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-creditos-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
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
    @Test void recebeConsultaEInspecionaCreditoSemPerderDecimalOuBytes() throws Exception {
        String token=token("0001"),key=open(token,202601);
        long number=number(receive(token,key,PAYLOAD));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        String body=http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.assunto").value("LANCAMENTO_CREDITO_TRIBUTARIO")).andExpect(jsonPath("$.recebidos").value(1))
            .andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0))
            .andExpect(jsonPath("$.registros[0].codigoLancamentoCreditoTributario").value("000009"))
            .andReturn().getResponse().getContentAsString();
        var exact=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(body);
        assertEquals(new BigDecimal("123456789012345678901234567890.12"),exact.get("registros").get(0).get("valorLancamentoCreditoTributario").decimalValue());
        assertArrayEquals(PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),exact.get("bytesBase64").binaryValue());
    }
    com.fasterxml.jackson.databind.node.ObjectNode root() throws Exception {
        return (com.fasterxml.jackson.databind.node.ObjectNode)new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(PAYLOAD);
    }
    com.fasterxml.jackson.databind.node.ObjectNode record(com.fasterxml.jackson.databind.node.ObjectNode root) {
        return (com.fasterxml.jackson.databind.node.ObjectNode)root.get("lancamentosCreditosTributarios").get(0);
    }
    @Test void schemaObrigatoriosOpcionaisTiposComprimentosEDatas() throws Exception {
        String token=token("0001"),key=open(token,202602);
        var schema=json.readTree(Files.readString(Path.of("docs/contratos/2026-v6-6.06/schemas.json"))).get("$defs").get("credito");
        var required=new java.util.HashSet<String>();schema.get("required").forEach(n->required.add(n.asText()));
        for (String name:required) {
            var root=root();record(root).remove(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            record(root).putNull(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        }
        for(var field:schema.get("properties").properties()) {
            String name=field.getKey();var definition=field.getValue();
            var root=root();var record=record(root);
            if (!required.contains(name)) {
                record.putNull(name);receive(token,key,root.toString()).andExpect(status().isOk());
                record.remove(name);receive(token,key,root.toString()).andExpect(status().isOk());
            }
            boolean numeric=definition.get("type").isTextual() && !definition.get("type").asText().equals("string");
            for(String invalid:numeric?new String[]{"\"1\"","true","[]","{}"}:new String[]{"1","true","[]","{}"}) {
                record.set(name,json.readTree(invalid));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
            if(definition.has("maxLength")) {
                int limit=definition.get("maxLength").asInt();record.put(name,"😀".repeat(limit));receive(token,key,root.toString()).andExpect(status().isOk());
                record.put(name,"😀".repeat(limit+1));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
                record.put(name,"");receive(token,key,root.toString()).andExpect(status().isOk());
            }
            if(definition.has("format")) for(String date:new String[]{"2020-02-30","2019-02-29","2020-2-01","2020-13-01","2020-02-01T00:00:00Z"}) {
                record.put(name,date);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
        }
    }
    @Test void dinheiroInteirosECompetenciaSemCoercaoOuTetoInventado() throws Exception {
        String token=token("0001"),key=open(token,202603);
        for(String amount:new String[]{"1","1.0","1.00","-1.20","0","1e2","100.000","0.00000000000000000000000000000000000000"})
            number(receive(token,key,PAYLOAD.replace("123456789012345678901234567890.12",amount)));
        for(String amount:new String[]{"1.001","1.005","0.001","0.01000000000000000000000000000000000001","\"1.00\"","true","null"})
            receive(token,key,PAYLOAD.replace("123456789012345678901234567890.12",amount)).andExpect(status().isUnprocessableEntity());
        for(String month:new String[]{"202000","202013","-202002","202002.5","1e1000000"})
            receive(token,key,PAYLOAD.replace("202002",month)).andExpect(status().isUnprocessableEntity());
        for(String month:new String[]{"199912","1.99912e5","12345678901234567890123456789012"})
            number(receive(token,key,PAYLOAD.replace("202002",month)));
        for(String name:new String[]{"anoLancamentoCreditoTributario","tipoOperacao","tipoCredito","codigoEspecificacaoNaturezaReceita"}) {
            var root=root();record(root).put(name,new java.math.BigInteger("123456789012345678901234567890"));number(receive(token,key,root.toString()));
            record(root).put(name,new BigDecimal("1.0"));number(receive(token,key,root.toString()));
            record(root).put(name,new BigDecimal("1.5"));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        }
    }
    @Test void extrasListasMistasERejeicoesNaoGeramLotes() throws Exception {
        String token=token("0001"),key=open(token,202604);
        for(String name:new String[]{"numeroInscricaoImobiliaria","inscricoesImobiliarias","lancamentoCreditoTributarioTransferencia","transferenciasCreditoTributario","extra"}) {
            var root=root();record(root).set(name,json.createArrayNode().addObject());receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        }
        for(String body:new String[]{"{\"lancamentosCreditosTributarios\":[]}","{\"lancamentosCreditosTributarios\":null}","{\"lancamentosCreditosTributarios\":[null]}",
                PAYLOAD.substring(0,PAYLOAD.length()-1)+",\"cadastrosContribuintes\":[]}"}) receive(token,key,body).andExpect(status().isUnprocessableEntity());
        for(String body:new String[]{PAYLOAD+PAYLOAD,PAYLOAD.replace("\"tipoCredito\":1","\"tipoCredito\":1,\"tipoCredito\":2")}) receive(token,key,body).andExpect(status().isBadRequest());
        long number=number(receive(token,key,PAYLOAD));
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.eventos.length()").value(2));
    }
    @Test void lotesSeparadosMesmoEnvioPreservamMultiplicidadeEIsolamento() throws Exception {
        String token=token("0001"),key=open(token,202605),other=token("0002");
        long contributor=number(receive(token,key,RemessaHttpTest.PAYLOAD));
        long first=number(receive(token,key,PAYLOAD)),second=number(receive(token,key,PAYLOAD));assertNotEquals(first,second);
        var root=root();root.withArray("lancamentosCreditosTributarios").add(record(root).deepCopy());long duplicated=number(receive(token,key,root.toString()));
        http.perform(get("/__simulator/lotes/"+duplicated).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.recebidos").value(2)).andExpect(jsonPath("$.registros.length()").value(2)).andExpect(jsonPath("$.ordem").value(4));
        http.perform(get("/__simulator/lotes/"+contributor).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andExpect(jsonPath("$.assunto").value("CADASTRO_CONTRIBUINTE"));
        receive(other,key,PAYLOAD).andExpect(status().isNotFound());receive("invalido",key,PAYLOAD).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+first).header("auth_token",other)).andExpect(status().isNotFound());
        http.perform(get("/__simulator/lotes/"+first)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+first).header("X-Simulator-Key",token)).andExpect(status().isUnauthorized());
    }

}
