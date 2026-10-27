package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.configuration.SimulatorApplication;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes=SimulatorApplication.class,webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties={"simulator.clock-mode=fixed","simulator.users[0].ugs=0001,0002"})
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReceptionTransportTest {
    static final String ADMIN="admin-local-ficticio",RECEIVE="/v6/tributario/enviarParcialLote?chavePacote=";
    @LocalServerPort int port;
    @Autowired javax.sql.DataSource source;
    final ObjectMapper json=new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) {
        properties.add("simulator.database",()->{try{return Files.createTempDirectory("esfinge-transporte-").resolve("state.sqlite").toString();}catch(Exception failure){throw new IllegalStateException();}});
    }
    record Response(int status,String body) {}
    byte[] raw(String method,String path,Map<String,String> headers,String body) throws Exception {
        byte[] content=body.getBytes(StandardCharsets.UTF_8);
        try(var socket=new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1",port),5000);socket.setSoTimeout(5000);
            var request=new StringBuilder(method+" "+path+" HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\nContent-Length: "+content.length+"\r\n");
            headers.forEach((name,value)->request.append(name).append(": ").append(value).append("\r\n"));
            socket.getOutputStream().write(request.append("\r\n").toString().getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().write(content);socket.getOutputStream().flush();
            return socket.getInputStream().readAllBytes();
        }
    }
    Response call(String method,String path,Map<String,String> headers,String body,int expected) throws Exception {
        String response=new String(raw(method,path,headers,body),StandardCharsets.UTF_8);
        assertTrue(response.startsWith("HTTP/1.1 "),response);int status=Integer.parseInt(response.substring(9,12));assertEquals(expected,status,response);
        String[] parts=response.split("\r\n\r\n",2);String payload=parts.length==2?parts[1]:"";
        if(parts[0].toLowerCase(Locale.ROOT).contains("transfer-encoding: chunked")) {
            var decoded=new StringBuilder();int offset=0;
            while(true) {int end=payload.indexOf("\r\n",offset),length=Integer.parseInt(payload.substring(offset,end),16);if(length==0)break;offset=end+2;decoded.append(payload,offset,offset+length);offset+=length+2;}payload=decoded.toString();
        }
        return new Response(status,payload);
    }
    Map<String,String> auth(String token) {return Map.of("auth_token",token,"Content-Type","application/json");}
    Map<String,String> admin() {return Map.of("X-Simulator-Key",ADMIN,"Content-Type","application/json");}
    String token(String ug) throws Exception {return json.readTree(call("POST","/autenticacao/login?codigoUg="+ug+"&descricaoEmpresaTI=Empresa&descritivoSoftware=Software",Map.of("codigoAcesso","acesso-ficticio-a","senha","senha-ficticia-a"),"",200).body()).get("chave").asText();}
    String open(String token) throws Exception {return json.readTree(call("GET","/v6/tributario/iniciarEnvio?anoMes=202601&cancelamentoAutomatico=NAO",auth(token),"",200).body()).get("chavePacote").asText();}
    String plan(String key,String subject) {return "/__simulator/falhas/recepcao?chavePacote="+key+"&assunto="+subject;}
    void configure(String key,String subject,String type,int attempt) throws Exception {call("PUT",plan(key,subject),admin(),"{\"tipo\":\""+type+"\",\"tentativa\":"+attempt+"}",200);}
    JdbcTemplate jdbc() {return new JdbcTemplate(source);}
    @Test void antesDaAceitacaoFechaConexaoSemConfirmacaoNemDadosEConsomeUmaTentativa() throws Exception {
        String token=token("0001"),key=open(token);configure(key,"CADASTRO_CONTRIBUINTE","ANTES_ACEITACAO",1);
        var before=jdbc().queryForList("SELECT atividade FROM remessas");long events=jdbc().queryForObject("SELECT COUNT(*) FROM remessa_eventos",Long.class);
        assertArrayEquals(new byte[0],raw("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD));
        assertEquals(0,jdbc().queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertEquals(0,jdbc().queryForObject("SELECT COUNT(*) FROM valores_recebidos",Integer.class));assertEquals(0,jdbc().queryForObject("SELECT COUNT(*) FROM processamento_trabalhos",Integer.class));
        assertEquals(before,jdbc().queryForList("SELECT atividade FROM remessas"));assertEquals(events,jdbc().queryForObject("SELECT COUNT(*) FROM remessa_eventos",Long.class));
        var state=json.readTree(call("GET",plan(key,"CADASTRO_CONTRIBUINTE"),admin(),"",200).body());assertEquals(1,state.get("tentativas").asInt());assertTrue(state.get("consumida").asBoolean());assertFalse(state.get("recuperacaoPendente").asBoolean());
        call("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD,200);assertEquals(1,jdbc().queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));
    }
    @Test void aposCommitPerdeSomenteRespostaBloqueiaAssuntoERecuperaComTotaisExatos() throws Exception {
        String token=token("0001"),key=open(token),subject="LANCAMENTO_CREDITO_TRIBUTARIO";configure(key,subject,"APOS_COMMIT",2);
        call("POST",RECEIVE+key,auth(token),CreditoHttpTest.PAYLOAD,200);
        call("POST",RECEIVE+key,auth(token),"{}",422);call("POST",RECEIVE+key,auth("invalido"),CreditoHttpTest.PAYLOAD,401);
        call("POST",RECEIVE+key,auth(token("0002")),CreditoHttpTest.PAYLOAD,404);
        assertEquals(1,json.readTree(call("GET",plan(key,subject),admin(),"",200).body()).get("tentativas").asInt());
        assertArrayEquals(new byte[0],raw("POST",RECEIVE+key,auth(token),CreditoHttpTest.PAYLOAD));
        assertEquals(2,jdbc().queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));long lost=jdbc().queryForObject("SELECT MAX(numero) FROM lotes",Long.class);
        assertEquals(0,jdbc().queryForObject("SELECT COUNT(*) FROM processamento_trabalhos",Integer.class));
        assertEquals(2,jdbc().queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='LOTE_RECEBIDO'",Integer.class));assertEquals(1,jdbc().queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='RESPOSTA_LOTE_PERDIDA'",Integer.class));
        var inspection=json.readTree(call("GET","/__simulator/lotes/"+lost,admin(),"",200).body());
        assertEquals("RECEBIDO",inspection.get("situacao").asText());assertArrayEquals(CreditoHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8),Base64.getDecoder().decode(inspection.get("bytesBase64").asText()));
        var before=JdbcFinalizationTest.snapshot(jdbc());var rejected=call("POST",RECEIVE+key,auth(token),CreditoHttpTest.PAYLOAD,409);assertEquals("SIM_ENVIO_ABERTO_SEM_PROCESSAMENTO",json.readTree(rejected.body()).get("codigo").asText());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc()));
        call("DELETE",plan(key,subject),admin(),"",409);call("PUT",plan(key,subject),admin(),"{\"tipo\":\"ANTES_ACEITACAO\",\"tentativa\":1}",409);
        call("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD,200);
        call("POST","/v6/tributario/descarteParcialPorAssunto?chavePacote="+key+"&assunto="+subject,auth(token),"",200);
        assertEquals(0,jdbc().queryForObject("SELECT recuperacao FROM falhas_recepcao",Integer.class));assertEquals(1,jdbc().queryForObject("SELECT consumida FROM falhas_recepcao",Integer.class));
        call("POST",RECEIVE+key,auth(token),CreditoHttpTest.PAYLOAD,200);call("POST",RECEIVE+key,auth(token),CreditoHttpTest.PAYLOAD,200);
        call("PUT","/__simulator/processamento",admin(),"{\"modo\":\"MANUAL\",\"latenciaMilissegundos\":0}",200);call("POST","/v6/tributario/iniciar-processamento?chavePacote="+key,auth(token),"",200);call("POST","/__simulator/processamento/avancar",admin(),"{\"milissegundos\":0,\"passos\":20}",200);
        var totals=json.createObjectNode().put("chavePacote",key);for(String name:FinalizacaoHttpTest.TOTALS)totals.put(name,"0");totals.put("quantidadeCadastroContribuinte","1").put("quantidadeLancamentoCreditosTributarios","2");
        call("POST","/v6/tributario/finalizarEnvio",auth(token),totals.toString(),200);
        assertEquals(3,jdbc().queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));assertEquals(2,jdbc().queryForObject("SELECT COUNT(*) FROM lotes WHERE ativo=0",Integer.class));
        assertEquals(1,jdbc().queryForObject("SELECT COUNT(DISTINCT hash) FROM lotes WHERE assunto=?",Integer.class,subject));assertTrue(jdbc().queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void administracaoEstritaNaoConsomePlanoNemRenovaAtividade() throws Exception {
        String token=token("0001"),key=open(token),route=plan(key,"CADASTRO_CONTRIBUINTE");var activity=jdbc().queryForList("SELECT atividade FROM remessas");
        call("PUT",route,auth(token),"{\"tipo\":\"APOS_COMMIT\",\"tentativa\":1}",401);call("GET",route,Map.of(),"",401);
        for(String body:new String[]{"{}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":0}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":1.0}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":\"1\"}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":9223372036854775808}","{\"tipo\":null,\"tentativa\":1}","{\"tipo\":\"OUTRO\",\"tentativa\":1}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":1,\"tentativa\":2}","{\"tipo\":\"APOS_COMMIT\",\"tentativa\":1} {}"}) call("PUT",route,admin(),body,400);
        call("GET",route,admin(),"",404);call("DELETE",route,admin(),"",404);call("PUT",plan(key,"OUTRO"),admin(),"{\"tipo\":\"APOS_COMMIT\",\"tentativa\":1}",422);
        configure(key,"CADASTRO_CONTRIBUINTE","APOS_COMMIT",2);var before=JdbcFinalizationTest.snapshot(jdbc());
        call("GET",route+"&extra=1",admin(),"",400);call("GET",route+"&assunto=CADASTRO_CONTRIBUINTE",admin(),"",400);call("GET",route,admin(),"x",400);call("DELETE",route,admin(),"x",400);
        call("GET",route,admin(),"",200);assertEquals(before,JdbcFinalizationTest.snapshot(jdbc()));assertEquals(activity,jdbc().queryForList("SELECT atividade FROM remessas"));
        call("DELETE",route,admin(),"",200);call("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD,200);
    }
    @Test void falhaRealDeCommitRetorna500EmVezDeInterromperERevertePlano() throws Exception {
        String token=token("0001"),key=open(token);configure(key,"CADASTRO_CONTRIBUINTE","APOS_COMMIT",1);
        jdbc().execute("CREATE TABLE falha_commit(id INTEGER REFERENCES remessas(id) DEFERRABLE INITIALLY DEFERRED)");jdbc().execute("CREATE TRIGGER impedir AFTER INSERT ON remessa_eventos WHEN NEW.tipo='RESPOSTA_LOTE_PERDIDA' BEGIN INSERT INTO falha_commit VALUES(-1); END");
        var before=JdbcFinalizationTest.snapshot(jdbc());var result=call("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD,500);assertEquals("SIM_ERRO_INTERNO",json.readTree(result.body()).get("codigo").asText());assertEquals(before,JdbcFinalizationTest.snapshot(jdbc()));
        for(String secret:List.of(token,key,"INSERT ","Exception","senha-ficticia-a")) assertFalse(result.body().contains(secret));
        jdbc().execute("DROP TRIGGER impedir");assertArrayEquals(new byte[0],raw("POST",RECEIVE+key,auth(token),RemessaHttpTest.PAYLOAD));assertEquals(1,jdbc().queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));
    }
}
