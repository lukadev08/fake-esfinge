package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes=SimulatorApplication.class,properties={"simulator.clock-mode=fixed"})
@AutoConfigureMockMvc(print=MockMvcPrint.NONE)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InspectionHttpTest {
    static final String ADMIN="admin-local-ficticio";
    @Autowired MockMvc http;
    @Autowired ObjectMapper json;
    @Autowired DataSource source;
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-inspection-").resolve("state.sqlite").toString();}catch(Exception failure){throw new IllegalStateException(failure);}});
    }
    String token() throws Exception {
        return json.readTree(http.perform(post("/autenticacao/login").header("codigoAcesso","acesso-ficticio-a").header("senha","senha-ficticia-a")
            .param("codigoUg","0001").param("descricaoEmpresaTI","Empresa").param("descritivoSoftware","Software"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chave").asText();
    }
    String open(String token) throws Exception {
        return json.readTree(http.perform(get("/v6/tributario/iniciarEnvio").header("auth_token",token).param("anoMes","202601").param("cancelamentoAutomatico","NAO"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("chavePacote").asText();
    }
    long receive(String token,String key) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(RemessaHttpTest.PAYLOAD))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    @Test void listaProtegidaFiltraSemCarregarPayloadENaoAlteraBanco() throws Exception {
        String token=token(),key=open(token);long lot=receive(token,key);
        var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(get("/__simulator/lotes").param("extra","1")).andExpect(status().isUnauthorized());
        http.perform(get("/__simulator/lotes").header("X-Simulator-Key",ADMIN).param("codigoUg","0001").param("anoMes","202601").param("assunto","CADASTRO_CONTRIBUINTE"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].numeroLote").value(lot)).andExpect(jsonPath("$.itens[0].codigoUg").value("0001"))
            .andExpect(jsonPath("$.itens[0].bytesBase64").doesNotExist()).andExpect(jsonPath("$.itens[0].registros").doesNotExist());
        http.perform(get("/__simulator/remessas").header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.itens[0].codigoUg").value("0001"));
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }

    com.fasterxml.jackson.databind.JsonNode read(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) throws Exception {
        return new ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(http.perform(request.header("X-Simulator-Key",ADMIN)).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }
    long receive(String token,String key,String payload) throws Exception {
        return json.readTree(http.perform(post("/v6/tributario/enviarParcialLote").header("auth_token",token).param("chavePacote",key).contentType("application/json").content(payload)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("numeroLote").asLong();
    }
    void advance() throws Exception {
        http.perform(post("/__simulator/processamento/avancar").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"milissegundos\":100000,\"passos\":30}")).andExpect(status().isOk());
    }
    @Test void paginaComTetoNaoIncluiNovasInsercoesERejeitaFiltroLimiteOuRecursoAlterados() throws Exception {
        String t=token(),k=open(t);long a=receive(t,k),b=receive(t,k),c=receive(t,k);
        var first=read(get("/__simulator/lotes").param("limite","1").param("codigoUg","0001"));assertEquals(a,first.at("/itens/0/numeroLote").asLong());
        String cursor=first.get("proximoCursor").asText();long d=receive(t,k);
        var second=read(get("/__simulator/lotes").param("limite","1").param("codigoUg","0001").param("cursor",cursor));assertEquals(b,second.at("/itens/0/numeroLote").asLong());
        var third=read(get("/__simulator/lotes").param("limite","1").param("codigoUg","0001").param("cursor",second.get("proximoCursor").asText()));assertEquals(c,third.at("/itens/0/numeroLote").asLong());assertTrue(third.get("proximoCursor").isNull());assertEquals(c,third.get("teto").asLong());
        assertEquals(4,read(get("/__simulator/lotes")).get("itens").size());assertTrue(d>c);
        for(var request:java.util.List.of(get("/__simulator/lotes").param("limite","2").param("codigoUg","0001"),get("/__simulator/lotes").param("limite","1").param("codigoUg","0002"),get("/__simulator/remessas").param("limite","1").param("codigoUg","0001")))
            http.perform(request.header("X-Simulator-Key",ADMIN).param("cursor",cursor)).andExpect(status().isBadRequest());
    }
    @Test void registrosDosSeisAssuntosPreservamIndicesTiposPrecisaoMultiplicidadeEBytes() throws Exception {
        String t=token(),k=open(t);
        for(String payload:new String[]{RemessaHttpTest.PAYLOAD,CreditoHttpTest.PAYLOAD,ArrecadacaoHttpTest.PAYLOAD,BaixaHttpTest.PAYLOAD,SaldoHttpTest.PAYLOAD,PrazoHttpTest.PAYLOAD}) {
            var root=new ObjectMapper().enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).readTree(payload);var array=(com.fasterxml.jackson.databind.node.ArrayNode)root.elements().next();var expected=array.get(0).deepCopy();array.add(expected.deepCopy());array.add(expected.deepCopy());String repeated=root.toString();long lot=receive(t,k,repeated);
            var first=read(get("/__simulator/lotes/"+lot+"/registros").param("tipo","RECEBIDOS").param("limite","2"));assertEquals(2,first.get("itens").size());assertEquals(expected,first.at("/itens/0/registro"));assertEquals(expected,first.at("/itens/1/registro"));assertEquals(1,first.at("/itens/1/indice").asInt());
            var last=read(get("/__simulator/lotes/"+lot+"/registros").param("tipo","RECEBIDOS").param("limite","2").param("cursor",first.get("proximoCursor").asText()));assertEquals(2,last.at("/itens/0/indice").asInt());assertEquals(expected,last.at("/itens/0/registro"));assertTrue(last.get("proximoCursor").isNull());
            var full=read(get("/__simulator/lotes/"+lot));assertArrayEquals(repeated.getBytes(java.nio.charset.StandardCharsets.UTF_8),full.get("bytesBase64").binaryValue());
        }
        http.perform(post("/v6/tributario/iniciar-processamento").header("auth_token",t).param("chavePacote",k)).andExpect(status().isOk());advance();
        for(var lot:read(get("/__simulator/lotes")).get("itens"))for(String type:new String[]{"PROCESSADOS","EFETIVOS"}) {
            var page=read(get("/__simulator/lotes/"+lot.get("numeroLote").asLong()+"/registros").param("tipo",type).param("limite","1"));assertEquals(1,page.get("itens").size());assertEquals(0,page.at("/itens/0/indice").asInt());assertFalse(page.get("proximoCursor").isNull());
        }
    }
    @Test void entradasEstritasSempreAutorizamAntesENaoEscrevem() throws Exception {
        String t=token(),k=open(t);long lot=receive(t,k);var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        for(String path:new String[]{"/__simulator/lotes","/__simulator/remessas","/__simulator/remessas/invalido","/__simulator/remessas/invalido/eventos","/__simulator/lotes/invalido/registros","/__simulator/relatorio"}) {
            for(String admin:new String[]{"",t,"errada"})http.perform(get(path).header("X-Simulator-Key",admin).param("extra","1").content("{}" )).andExpect(status().isUnauthorized());
        }
        for(String query:new String[]{"limite=0","limite=-1","limite=201","limite=1.0","limite=01","limite=9223372036854775808","limite=1&limite=1","anoMes=202513","anoMes=202613","anoMes=202601.0","codigoUg=","assunto=outro","cursor=???","extra=1"})http.perform(get("/__simulator/lotes?"+query).header("X-Simulator-Key",ADMIN)).andExpect(status().isBadRequest());
        for(String path:new String[]{"/__simulator/lotes","/__simulator/remessas","/__simulator/remessas/1","/__simulator/remessas/1/eventos","/__simulator/lotes/"+lot+"/registros?tipo=RECEBIDOS","/__simulator/relatorio?origem=FICTICIA&formato=LOCAL"})http.perform(get(path).header("X-Simulator-Key",ADMIN).content("{}" )).andExpect(status().isBadRequest());
        http.perform(get("/__simulator/lotes/"+lot+"/registros").header("X-Simulator-Key",ADMIN)).andExpect(status().isBadRequest());
        http.perform(get("/__simulator/lotes").header("X-Simulator-Key",ADMIN).param("cenario","outro")).andExpect(status().isNotFound());
        http.perform(get("/__simulator/remessas/999").header("X-Simulator-Key",ADMIN)).andExpect(status().isNotFound());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void relatorioLocalIncluiEventosReaisPaginasManifestoEOrigemDeclarada() throws Exception {
        String t=token(),k=open(t);long lot=receive(t,k);receive(t,k);var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        var first=read(get("/__simulator/relatorio").param("origem","ORIGINAL").param("formato","LOCAL").param("limite","1"));
        assertEquals("DECLARADA_NAO_VERIFICADA",first.at("/origem/natureza").asText());assertEquals("NAO_COMPARADO",first.get("comparacao").asText());assertTrue(first.get("divergencias").isNull());
        assertEquals("ENVIO_ABERTO",first.at("/sequencia/itens/0/tipo").asText());assertTrue(first.at("/sequencia/itens/0/numeroLote").isNull());
        String cursor=first.at("/sequencia/proximoCursor").asText();var second=read(get("/__simulator/relatorio").param("origem","ORIGINAL").param("formato","LOCAL").param("limite","1").param("cursor",cursor));
        assertEquals(lot,second.at("/sequencia/itens/0/numeroLote").asLong());assertEquals(1,second.at("/sequencia/itens/0/identificadorRemessa").asLong());assertTrue(second.at("/sequencia/itens/0/id").asLong()>first.at("/sequencia/itens/0/id").asLong());
        byte[] coverage=Files.readAllBytes(java.nio.file.Path.of("docs/cobertura.md"));assertEquals(new String(coverage,java.nio.charset.StandardCharsets.UTF_8),first.at("/manifesto/conteudo").asText());assertEquals(java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(coverage)),first.at("/manifesto/sha256").asText());
        http.perform(get("/__simulator/relatorio").header("X-Simulator-Key",ADMIN).param("origem","FICTICIA").param("formato","LOCAL").param("limite","1").param("cursor",cursor)).andExpect(status().isBadRequest());
        assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
    @Test void sinteseCompartilhavelExcluiDadosDaExecucaoEParametrosInaplicaveis() throws Exception {
        String t=token(),k=open(t);receive(t,k);var response=read(get("/__simulator/relatorio").param("origem","ORIGINAL").param("formato","COMPARTILHAVEL"));
        assertEquals(java.util.Set.of("versaoFormato","artefato","contrato","manifesto","comparacao","divergencias","limites"),new java.util.HashSet<String>(){{response.fieldNames().forEachRemaining(this::add);}});
        String text=response.toString();for(String secret:new String[]{k,t,ADMIN,"cenario-ficticio-a","Pessoa Ficticia","acesso-ficticio-a","senha-ficticia-a"})assertFalse(text.contains(secret),secret);
        for(String query:new String[]{"origem=DETECTADA&formato=LOCAL","formato=LOCAL","origem=FICTICIA&formato=CSV","origem=FICTICIA&formato=COMPARTILHAVEL&limite=1","origem=FICTICIA&formato=COMPARTILHAVEL&cursor=x","origem=FICTICIA&formato=LOCAL&caminho=/tmp/x"})http.perform(get("/__simulator/relatorio?"+query).header("X-Simulator-Key",ADMIN)).andExpect(status().isBadRequest());
    }
    @Test void resetInvalidaTodosOsCursoresAntesDeRecursoRemovidoERelatorioFicaVazio() throws Exception {
        String t=token(),k=open(t);long lot=receive(t,k, "{\"cadastrosContribuintes\":["+RemessaHttpTest.PAYLOAD.substring(RemessaHttpTest.PAYLOAD.indexOf('[')+1,RemessaHttpTest.PAYLOAD.length()-2)+","+RemessaHttpTest.PAYLOAD.substring(RemessaHttpTest.PAYLOAD.indexOf('[')+1,RemessaHttpTest.PAYLOAD.length()-2)+"]}");receive(t,k);
        var lots=read(get("/__simulator/lotes").param("limite","1"));var records=read(get("/__simulator/lotes/"+lot+"/registros").param("tipo","RECEBIDOS").param("limite","1"));var events=read(get("/__simulator/remessas/1/eventos").param("limite","1"));var report=read(get("/__simulator/relatorio").param("origem","FICTICIA").param("formato","LOCAL").param("limite","1"));
        http.perform(post("/__simulator/reset").header("X-Simulator-Key",ADMIN).contentType("application/json").content("{\"cenario\":\"cenario-ficticio-a\",\"geracaoEsperada\":0}")).andExpect(status().isOk());
        for(var request:java.util.List.of(get("/__simulator/lotes").param("cursor",lots.get("proximoCursor").asText()),get("/__simulator/lotes/"+lot+"/registros").param("tipo","RECEBIDOS").param("cursor",records.get("proximoCursor").asText()),get("/__simulator/remessas/1/eventos").param("cursor",events.get("proximoCursor").asText()),get("/__simulator/relatorio").param("origem","FICTICIA").param("formato","LOCAL").param("cursor",report.at("/sequencia/proximoCursor").asText())))http.perform(request.header("X-Simulator-Key",ADMIN).param("limite","1")).andExpect(status().isConflict());
        var empty=read(get("/__simulator/relatorio").param("origem","NAO_INFORMADA").param("formato","LOCAL"));assertEquals(0,empty.at("/sequencia/itens").size());assertEquals(1,empty.at("/sequencia/geracao").asInt());assertEquals(0,empty.get("resultados").size());
    }

    @Test void limitesLocaisPadraoMaximoVazioE422EstritoPermanecemExplicitos() throws Exception {
        assertEquals(0,read(get("/__simulator/lotes")).get("itens").size());String t=token(),k=open(t);for(int i=0;i<51;i++)receive(t,k);
        var standard=read(get("/__simulator/lotes"));assertEquals(50,standard.get("itens").size());assertFalse(standard.get("proximoCursor").isNull());
        assertEquals(51,read(get("/__simulator/lotes").param("limite","200")).get("itens").size());
        var jdbc=new JdbcTemplate(source);var before=JdbcFinalizationTest.snapshot(jdbc);
        http.perform(post("/v6/tributario/descartar-lote").header("auth_token",t)).andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("SIM_COBERTURA_AUSENTE"));assertEquals(before,JdbcFinalizationTest.snapshot(jdbc));
    }
}
