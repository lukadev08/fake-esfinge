package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SimulatorApplication.class, properties="simulator.users[0].ugs=0001,0002")
@AutoConfigureMockMvc(print = org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint.NONE)
class RemessaHttpTest {
    static final String PAYLOAD="{\"cadastrosContribuintes\":[{\"numeroMatriculaContribuinte\":\"0007\",\"dataAtualizacaoCadastral\":\"2026-01-02\",\"tipoContribuinte\":\"1\",\"numeroDocumentoContribuinte\":\"00000000000\",\"nomeCompletoContribuinte\":\"Pessoa Ficticia\",\"indicativoCadastroAtivo\":\"S\",\"indicativoEspolio\":\"N\"}]}";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) {
        properties.add("simulator.database", () -> {
            try { return Files.createTempDirectory("esfinge-remessas-").resolve("state.sqlite").toString(); }
            catch (Exception e) { throw new IllegalStateException(); }
        });
    }
    String token() throws Exception { return token("0001"); }
    String token(String ug) throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso", "acesso-ficticio-a")
                .header("senha", "senha-ficticia-a").param("codigoUg", ug).param("descricaoEmpresaTI", "Empresa")
                .param("descritivoSoftware", "Software")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token,int month) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",Integer.toString(month))
                .param("cancelamentoAutomatico","NAO")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    @Test void abreEnvioComChaveOpaca() throws Exception {
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token", token()).param("anoMes", "202601")
                .param("cancelamentoAutomatico", "NAO")).andExpect(status().isOk()).andExpect(jsonPath("$.chavePacote").isString());
    }
    @Test void politicaOmitidaOuForaDeNaoRetorna422() throws Exception {
        String token=token();
        for (String policy:new String[]{"SIM","","outro"})
            http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes","202602")
                    .param("cancelamentoAutomatico",policy)).andExpect(status().isUnprocessableEntity());
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes","202602"))
                .andExpect(status().isUnprocessableEntity());
    }
    @Test void recebeContribuinteEmRecebido() throws Exception {
        String token=token(), key=open(token,202604);
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
                .contentType("application/json").content(PAYLOAD)).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("RECEBIDO")).andExpect(jsonPath("$.numeroLote").isNumber())
                .andExpect(jsonPath("$.identificadorRemessaOnline").isNumber()).andExpect(jsonPath("$.dataEnvio").isString());
    }
    @Test void consultaNaoProcessaEAdminConservaBytes() throws Exception {
        String token=token(), key=open(token,202605);
        long number=json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
                .contentType("application/json").content(PAYLOAD)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        for (int i=0;i<2;i++) http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("RECEBIDO"))
                .andExpect(jsonPath("$.identificadorRemessa").isNumber()).andExpect(jsonPath("$.identificadorRemessaOnline").doesNotExist());
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bytesBase64").value(java.util.Base64.getEncoder().encodeToString(PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8))))
                .andExpect(jsonPath("$.registros[0].numeroMatriculaContribuinte").value("0007"))
                .andExpect(jsonPath("$.recebidos").value(1)).andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0))
                .andExpect(jsonPath("$.eventos[0].numeroLote").isEmpty());
    }
    @Test void contentTypeJsonComCharsetEntreAspasEPermitido() throws Exception {
        String token=token(),key=open(token,202606);
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key)
                .contentType("application/json; charset=\"UTF-8\"").content(PAYLOAD)).andExpect(status().isOk());
    }
    org.springframework.test.web.servlet.ResultActions receive(String token,String key,String body) throws Exception {
        return http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(body));
    }
    @Test void schemaDocumentalControlaObrigatoriosTiposNulosComprimentosEUnicode() throws Exception {
        String token=token(),key=open(token,202607);
        var schema=json.readTree(Files.readString(java.nio.file.Path.of("docs/contratos/2026-v6-6.06/schemas.json"))).get("$defs").get("contribuinte");
        var required=new java.util.HashSet<String>();schema.get("required").forEach(n->required.add(n.asText()));
        for(var name:required) {
            var root=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(PAYLOAD);var record=(com.fasterxml.jackson.databind.node.ObjectNode)root.get("cadastrosContribuintes").get(0);
            record.remove(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            record.putNull(name);receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
        }
        var properties=schema.get("properties").properties().iterator();
        while(properties.hasNext()) {
            var property=properties.next();String name=property.getKey();
            for(String invalid:new String[]{"1","true","[]","{}"}) {
                var root=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(PAYLOAD);var record=(com.fasterxml.jackson.databind.node.ObjectNode)root.get("cadastrosContribuintes").get(0);
                record.set(name,json.readTree(invalid));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
            }
            var root=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(PAYLOAD);var record=(com.fasterxml.jackson.databind.node.ObjectNode)root.get("cadastrosContribuintes").get(0);
            if(!required.contains(name)) {record.putNull(name);receive(token,key,root.toString()).andExpect(status().isOk());record.remove(name);receive(token,key,root.toString()).andExpect(status().isOk());}
            if(property.getValue().has("maxLength")) {
                int limit=property.getValue().get("maxLength").asInt();record.put(name,"😀".repeat(limit));receive(token,key,root.toString()).andExpect(status().isOk());
                record.put(name,"😀".repeat(limit+1));receive(token,key,root.toString()).andExpect(status().isUnprocessableEntity());
                record.put(name,"");receive(token,key,root.toString()).andExpect(status().isOk());
            }
        }
        for(String date:new String[]{"2026-02-30","2026-1-02","2026-01-02T00:00:00Z","2026-13-01","2025-02-29"})
            receive(token,key,PAYLOAD.replace("2026-01-02",date)).andExpect(status().isUnprocessableEntity());
    }
    @Test void jsonMalformadoDuplicadoExtraListasInvalidasEAssuntosSemSuporteNaoPersistem() throws Exception {
        String token=token(),key=open(token,202608);
        for(String body:new String[]{"{","",PAYLOAD+PAYLOAD,PAYLOAD.replace("\"0007\"","\"0007\",\"numeroMatriculaContribuinte\":\"outra\"")})
            receive(token,key,body).andExpect(status().isBadRequest());
        http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json")
                .content(new byte[]{(byte)0xc3,0x28})).andExpect(status().isBadRequest());
        for(String body:new String[]{"{}","[]","null","{\"cadastrosContribuintes\":[]}","{\"cadastrosContribuintes\":null}",
                "{\"lote\":"+PAYLOAD+"}",PAYLOAD.substring(0,PAYLOAD.length()-1)+",\"lancamentosCreditosTributarios\":[]}",
                PAYLOAD.replace("\"numeroMatriculaContribuinte\":","\"extra\":1,\"numeroMatriculaContribuinte\":"),"{\"desconhecido\":[{}]}"})
            receive(token,key,body).andExpect(status().isUnprocessableEntity());
        for(String subject:new String[]{"baixasCreditosTributarios","saldosIniciaisCreditoTributario","inicioPrazoPrescricional"})
            receive(token,key,"{\""+subject+"\":[{}]}").andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SIM_COBERTURA_AUSENTE"));
        long number=json.readTree(receive(token,key,PAYLOAD).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ordem").value(1)).andExpect(jsonPath("$.eventos.length()").value(2));
    }
    @Test void multiplosLotesIdenticosE10001RegistrosSemLimiteInventado() throws Exception {
        String token=token(),key=open(token,202609);
        long first=json.readTree(receive(token,key,PAYLOAD).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        long second=json.readTree(receive(token,key,PAYLOAD).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        org.junit.jupiter.api.Assertions.assertNotEquals(first,second);
        var body=json.createObjectNode();var records=body.putArray("cadastrosContribuintes");var record=json.readTree(PAYLOAD).get("cadastrosContribuintes").get(0);
        for(int i=0;i<10001;i++)records.add(record);
        long number=json.readTree(receive(token,key,body.toString()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key","admin-local-ficticio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.recebidos").value(10001)).andExpect(jsonPath("$.ordem").value(3))
                .andExpect(jsonPath("$.processados").value(0)).andExpect(jsonPath("$.efetivos").value(0));
    }
    @Test void isolamentoUgAdminAutenticacaoEParametros() throws Exception {
        String token=token(),other=token("0002"),key=open(token,202610);
        long number=json.readTree(receive(token,key,PAYLOAD).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
        receive(other,key,PAYLOAD).andExpect(status().isNotFound());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/"+number).header("auth_token",other)).andExpect(status().isNotFound());
        http.perform(get("/__simulator/lotes/"+number)).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes/"+number).header("X-Simulator-Key",token)).andExpect(status().isUnauthorized());
        for(String month:new String[]{"202600","202613","202701","abc","202601.0"})
            http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes",month).param("cancelamentoAutomatico","NAO")).andExpect(status().isBadRequest());
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes","202611","202612").param("cancelamentoAutomatico","NAO")).andExpect(status().isBadRequest());
        http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token","invalido").param("anoMes","202611").param("cancelamentoAutomatico","NAO")).andExpect(status().isUnauthorized());
        receive("invalido",key,PAYLOAD).andExpect(status().isUnauthorized());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/invalido").header("auth_token",token)).andExpect(status().isBadRequest());
        http.perform(get("/v6/tributario/consultarPorNumeroLote/-1").header("auth_token",token)).andExpect(status().isNotFound());
        http.perform(get("/rota-inexistente").header("auth_token",token)).andExpect(status().isNotFound());
    }
}
