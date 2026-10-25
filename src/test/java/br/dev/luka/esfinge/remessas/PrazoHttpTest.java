package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
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
class PrazoHttpTest {
    static final String YEAR="anoLancamentoCreditoTributarioOuDividaAtiva",CODE="codigoLancamentoCreditoTributarioOuNumeroInscricaoDividaAtiva";
    static final String START="dataInicioPrazoPrescricional",UPDATE="dataAtualizacaoPrazoPrescricional";
    static final String RECORD="{\""+YEAR+"\":2015,\""+CODE+"\":\"000007\",\""+START+"\":\"2020-02-29\",\""+UPDATE+"\":\"2010-01-01\"}";
    static final String PAYLOAD="{\"inicioPrazoPrescricional\":["+RECORD+"]}";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void database(DynamicPropertyRegistry p) {
        p.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-prazo-").resolve("state.sqlite").toString();}catch(Exception e){throw new IllegalStateException();}});
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
    @Test void recebeConsultaEInspecionaPrazoSemValidacaoFiscal() throws Exception {
        String token=token("0001"),key=open(token,202601);long number=number(receive(token,key,PAYLOAD));
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",token)).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        String body=http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.assunto").value("INICIO_PRAZO_PRESCRICIONAL")).andExpect(jsonPath("$.recebidos").value(1))
            .andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0)).andReturn().getResponse().getContentAsString();
        var snapshot=json.readTree(body);var record=snapshot.get("registros").get(0);
        assertEquals(2015,record.get(YEAR).asInt());assertEquals("000007",record.get(CODE).asText());
        assertEquals("2020-02-29",record.get(START).asText());assertEquals("2010-01-01",record.get(UPDATE).asText());
        assertArrayEquals(PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8),snapshot.get("bytesBase64").binaryValue());
    }
    com.fasterxml.jackson.databind.node.ObjectNode root() throws Exception { return (com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(PAYLOAD); }
    com.fasterxml.jackson.databind.node.ObjectNode record(com.fasterxml.jackson.databind.node.ObjectNode root) {
        return (com.fasterxml.jackson.databind.node.ObjectNode)root.get("inicioPrazoPrescricional").get(0);
    }
    @Test void schemaFechadoRejeitaObrigatoriosNulosAusentesETiposSemCoercao() throws Exception {
        String token=token("0001"),key=open(token,202602);
        var schema=json.readTree(Files.readString(Path.of("docs/contratos/2026-v6-6.06/schemas.json"))).get("$defs").get("prazo");
        for(var name:schema.get("required")) {
            var root=root();record(root).remove(name.asText());receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            record(root).putNull(name.asText());receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            for(String invalid:name.asText().equals(YEAR)?new String[]{"\"2015\"","true","[]","{}","2015.01"}:new String[]{"2015","true","[]","{}"}) {
                root=root();record(root).set(name.asText(),json.readTree(invalid));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
        }
        var extra=root();record(extra).put("extra","ficticio");receive(token,key,extra.toString()).andExpect(status().isUnprocessableEntity());
        long id=number(receive(token,key,PAYLOAD));
        http.perform(get("/__simulator/lotes/"+id).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.eventos.length()").value(2));
    }
    @Test void ambasDatasExigemFormatoECalendarioSemRegraDeCronologia() throws Exception {
        String token=token("0001"),key=open(token,202603);
        for(String name:new String[]{START,UPDATE}) {
            for(String invalid:new String[]{"2025-02-29","1900-02-29","2026-02-30","2026-13-01","2026-00-01","2026-01-00","2026-1-01","26-01-01","2026-01-01T00:00:00Z","+2026-01-01","２０２６-01-01"}) {
                var root=root();record(root).put(name,invalid);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
            var root=root();record(root).put(name,"2000-02-29");number(receive(token,key,root.toString()));
        }
        number(receive(token,key,PAYLOAD));
    }
    @Test void identificadorUnicodeEAnoInteiroGrandeSaoPreservados() throws Exception {
        String token=token("0001"),key=open(token,202604);var root=root();
        record(root).put(CODE,"😀".repeat(16));long unicode=number(receive(token,key,root.toString()));
        var saved=json.readTree(http.perform(get("/__simulator/lotes/"+unicode).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertEquals("😀".repeat(16),saved.get("registros").get(0).get(CODE).asText());
        record(root).put(CODE,"😀".repeat(17));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        record(root).put(CODE,"");number(receive(token,key,root.toString()));
        for(String integer:new String[]{"123456789012345678901234567890","1.0","1e2","-1","0"}) {
            root=root();record(root).set(YEAR,new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(integer));
            long id=number(receive(token,key,root.toString()));
            var snapshot=json.readTree(http.perform(get("/__simulator/lotes/"+id).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
            assertEquals(0,new java.math.BigDecimal(integer).compareTo(snapshot.get("registros").get(0).get(YEAR).decimalValue()));
        }
    }
    @Test void envelopeDuplicatasSeisAssuntosEIsolamentoPreservamContagens() throws Exception {
        String token=token("0001"),key=open(token,202605),other=token("0002");
        for(String body:new String[]{"{\"inicioPrazoPrescricional\":[]}","{\"inicioPrazoPrescricional\":null}","{\"inicioPrazoPrescricional\":[null]}",
                "{\"inicioPrazoPrescricional\":[[]]}","{\"lote\":"+PAYLOAD+"}",PAYLOAD.substring(0,PAYLOAD.length()-1)+",\"cadastrosContribuintes\":[]}"})
            receive(token,key,body).andExpect(status().isUnprocessableEntity());
        for(String body:new String[]{PAYLOAD+PAYLOAD,PAYLOAD.replace("\""+YEAR+"\":2015","\""+YEAR+"\":2015,\""+YEAR+"\":2016")})
            receive(token,key,body).andExpect(status().isBadRequest());
        long first=number(receive(token,key,PAYLOAD)),second=number(receive(token,key,PAYLOAD));assertNotEquals(first,second);
        var root=root();root.withArray("inicioPrazoPrescricional").add(record(root).deepCopy());long duplicate=number(receive(token,key,root.toString()));
        http.perform(get("/__simulator/lotes/"+duplicate).header("X-Simulator-Key","admin-local-ficticio")).andExpect(status().isOk())
            .andExpect(jsonPath("$.recebidos").value(2)).andExpect(jsonPath("$.registros.length()").value(2)).andExpect(jsonPath("$.ordem").value(3));
        for(String payload:new String[]{RemessaHttpTest.PAYLOAD,CreditoHttpTest.PAYLOAD,ArrecadacaoHttpTest.PAYLOAD,BaixaHttpTest.PAYLOAD,SaldoHttpTest.PAYLOAD}) number(receive(token,key,payload));
        receive(other,key,PAYLOAD).andExpect(status().isNotFound());receive("invalido",key,PAYLOAD).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+first).header("auth_token",other)).andExpect(status().isNotFound());
        http.perform(get("/__simulator/lotes/"+first)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+first).header("X-Simulator-Key",token)).andExpect(status().isUnauthorized());
    }
}
