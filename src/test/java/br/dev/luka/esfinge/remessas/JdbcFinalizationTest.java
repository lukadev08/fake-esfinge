package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.autenticacao.adapter.out.jdbc.*;
import br.dev.luka.esfinge.autenticacao.application.AuthenticationService;
import br.dev.luka.esfinge.autenticacao.domain.AuthenticationFailure;
import br.dev.luka.esfinge.cenarios.adapter.out.jdbc.JdbcScenarioRepository;
import br.dev.luka.esfinge.cenarios.application.ScenarioService;
import br.dev.luka.esfinge.configuration.*;
import br.dev.luka.esfinge.remessas.adapter.in.http.LotePayload;
import br.dev.luka.esfinge.remessas.adapter.out.jdbc.*;
import br.dev.luka.esfinge.remessas.adapter.out.json.StructuralRecords;
import br.dev.luka.esfinge.remessas.application.*;
import br.dev.luka.esfinge.remessas.domain.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.sqlite.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class JdbcFinalizationTest {
    @TempDir Path temporary;
    static final Instant INITIAL=Instant.parse("2026-01-01T00:00:00Z");
    static final String ADMIN="admin-ficticio";
    SimulatorSettings settings(String scenario) {return new SimulatorSettings(temporary.resolve("state.sqlite").toString(),scenario,"2026/V6/6.06",ADMIN,"fixed",1,INITIAL,Duration.ofHours(1),List.of(new SimulatorSettings.User("usuario-ficticio","acesso-ficticio","senha-ficticia",Set.of("0001","0002"))));}
    record Fixture(DataSource source,AuthenticationService auth,RemessaService remessas,ProcessingService processing,JdbcBusinessClock clock) {
        String token(String ug) {return auth.login("acesso-ficticio","senha-ficticia",ug);}
        Lote receive(String token,String key,String payload) {return remessas.receive(token,key,new LotePayload().read(payload.getBytes(StandardCharsets.UTF_8)));}
    }
    Fixture fixture(String scenario) {
        var source=SqliteDatabase.open(settings(scenario));var clock=new JdbcBusinessClock(source,scenario,Clock.fixed(INITIAL,ZoneOffset.UTC));
        var auth=new AuthenticationService(new JdbcAuthenticationRepository(source),clock,scenario,Duration.ofHours(1));
        var inspection=new ScenarioService(new JdbcScenarioRepository(source),scenario,ADMIN,auth);var repository=new JdbcRemessaRepository(source);var tx=new JdbcTransactions(source);
        var remessas=new RemessaService(auth,repository,tx,clock,inspection,new JdbcProcessingRepository(source));
        return new Fixture(source,auth,remessas,new ProcessingService(auth,inspection,remessas,repository,new JdbcProcessingRepository(source),tx,clock,clock,new StructuralRecords(),scenario),clock);
    }
    static Finalization totals(long count) {
        var values=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var subject:Assunto.values()) values.put(subject,java.math.BigInteger.ZERO);
        values.put(Assunto.CADASTRO_CONTRIBUINTE,java.math.BigInteger.valueOf(count));return new Finalization(values);
    }
    void complete(Fixture f,String token,String key) {f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);f.processing.start(token,key);f.processing.advance(ADMIN,0,100);}
    @Test void totalEfetivoPreserva10001DuplicatasEReinicioNaoReabre() throws Exception {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();
        var json=new com.fasterxml.jackson.databind.ObjectMapper();var root=json.readTree(RemessaHttpTest.PAYLOAD);String record=root.get("cadastrosContribuintes").get(0).toString();
        String payload="{\"cadastrosContribuintes\":["+String.join(",",Collections.nCopies(10001,record))+"]}";
        var lot=f.receive(token,key,payload);complete(f,token,key);f.remessas.finish(token,key,totals(10001));
        var jdbc=new JdbcTemplate(f.source);var saved=snapshot(jdbc);var restarted=fixture("cenario-a");
        assertEquals(saved,snapshot(jdbc));assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->restarted.remessas.finish(token,key,totals(10001))).kind());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->restarted.receive(token,key,RemessaHttpTest.PAYLOAD)).kind());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->restarted.processing.start(token,key)).kind());
        assertEquals(10001,restarted.processing.inspect(ADMIN,lot.number()).effective().size());assertArrayEquals(payload.getBytes(StandardCharsets.UTF_8),restarted.remessas.inspect(ADMIN,lot.number()).lote().bytes());
        assertEquals(0,restarted.processing.advance(ADMIN,1,100).passosExecutados());
        var before=snapshot(jdbc);restarted.processing.consult(token,lot.number());assertEquals(before,snapshot(jdbc));
        String newKey=restarted.remessas.open(token,202601,"NAO").key();assertNotEquals(key,newKey);
        var next=restarted.receive(token,newKey,RemessaHttpTest.PAYLOAD);assertTrue(next.number()>lot.number());complete(restarted,token,newKey);restarted.remessas.finish(token,newKey,totals(1));
        assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void totaisNaoUsamOutrosEnviosUgsOuCenarios() {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();a.receive(token,key,RemessaHttpTest.PAYLOAD);complete(a,token,key);
        String another=a.remessas.open(token,202602,"NAO").key();a.receive(token,another,RemessaHttpTest.PAYLOAD);complete(a,token,another);
        String ug=a.token("0002"),ugKey=a.remessas.open(ug,202601,"NAO").key();a.receive(ug,ugKey,RemessaHttpTest.PAYLOAD);complete(a,ug,ugKey);
        var b=fixture("cenario-b");String other=b.token("0001"),otherKey=b.remessas.open(other,202601,"NAO").key();b.receive(other,otherKey,RemessaHttpTest.PAYLOAD);complete(b,other,otherKey);
        var jdbc=new JdbcTemplate(a.source);var before=snapshot(jdbc);
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->a.remessas.finish(ug,key,totals(1))).kind());
        assertEquals(RemessaFailure.Kind.NOT_FOUND,assertThrows(RemessaFailure.class,()->b.remessas.finish(other,key,totals(1))).kind());
        assertEquals(before,snapshot(jdbc));a.remessas.finish(token,key,totals(1));
        assertEquals(4,jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos",Integer.class));assertEquals(3,jdbc.queryForObject("SELECT COUNT(*) FROM remessas WHERE finalizada_em IS NULL",Integer.class));
    }
    @Test void finalizacoesConcorrentesConcluemUmaVezEAberturasRespeitamUnicidade() throws Exception {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();a.receive(token,key,RemessaHttpTest.PAYLOAD);complete(a,token,key);var b=fixture("cenario-a");
        var results=race(()->{a.remessas.finish(token,key,totals(1));return "ok";},()->{b.remessas.finish(token,key,totals(1));return "ok";});
        assertEquals(1,results.stream().filter("ok"::equals).count());assertEquals(1,results.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());
        var keys=race(()->a.remessas.open(token,202601,"NAO").key(),()->b.remessas.open(token,202601,"NAO").key());
        assertEquals(1,keys.stream().filter(RemessaFailure.Kind.CONFLICT::equals).count());
        var jdbc=new JdbcTemplate(a.source);assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_FINALIZADO'",Integer.class));assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessas WHERE finalizada_em IS NULL",Integer.class));
    }
    @Test void finalizacaoConcorrenteComRecepcaoNaoPerdeLote() throws Exception {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();a.receive(token,key,RemessaHttpTest.PAYLOAD);complete(a,token,key);var b=fixture("cenario-a");
        var result=race(()->{a.remessas.finish(token,key,totals(1));return "closed";},()->{b.receive(token,key,RemessaHttpTest.PAYLOAD);return "received";});
        assertTrue(result.contains(RemessaFailure.Kind.CONFLICT));var jdbc=new JdbcTemplate(a.source);
        if(result.contains("closed")) {assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertNotNull(jdbc.queryForObject("SELECT finalizada_em FROM remessas",String.class));}
        else {assertTrue(result.contains("received"));assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM lotes",Integer.class));assertNull(jdbc.queryForObject("SELECT finalizada_em FROM remessas",String.class));complete(a,token,key);a.remessas.finish(token,key,totals(2));}
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_FINALIZADO'",Integer.class));assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void finalizacaoConcorrenteComUltimaTransicaoOuAberturaNaoProduzEfeitosTardios() throws Exception {
        var a=fixture("cenario-a");String token=a.token("0001"),key=a.remessas.open(token,202601,"NAO").key();a.processing.configure(ADMIN,Processing.Mode.MANUAL,0);a.receive(token,key,RemessaHttpTest.PAYLOAD);a.processing.start(token,key);a.processing.advance(ADMIN,0,1);var b=fixture("cenario-a");
        var result=race(()->{a.remessas.finish(token,key,totals(1));return "closed";},()->{b.processing.advance(ADMIN,0,1);return "processed";});
        assertTrue(result.contains("processed"));if(!result.contains("closed")) {assertTrue(result.contains(RemessaFailure.Kind.CONFLICT));a.remessas.finish(token,key,totals(1));}
        var jdbc=new JdbcTemplate(a.source);var before=snapshot(jdbc);assertEquals(0,b.processing.advance(ADMIN,0,10).passosExecutados());assertEquals(before,snapshot(jdbc));
        String next=a.remessas.open(token,202601,"NAO").key();a.receive(token,next,RemessaHttpTest.PAYLOAD);complete(a,token,next);
        var reopened=race(()->{a.remessas.finish(token,next,totals(1));return "closed";},()->b.remessas.open(token,202601,"NAO").key());assertTrue(reopened.contains("closed"));
        assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM remessa_eventos WHERE tipo='ENVIO_FINALIZADO'",Integer.class));
    }
    static List<Object> race(Callable<Object> a,Callable<Object> b) throws Exception {
        try(var pool=Executors.newFixedThreadPool(2)) {var gate=new CountDownLatch(1);var futures=new ArrayList<Future<Object>>();
            for(var action:List.of(a,b)) futures.add(pool.submit(()->{gate.await();try{return action.call();}catch(RemessaFailure failure){return failure.kind();}}));
            gate.countDown();var result=new ArrayList<Object>();for(var future:futures) result.add(future.get(30,TimeUnit.SECONDS));return result;
        }
    }
    @Test void marcadorRevalidadoDuranteCalculoImpedeEfeitosEConsultaHistoricaNaoToca() {
        var f=fixture("cenario-a");String token=f.token("0001"),key=f.remessas.open(token,202601,"NAO").key();f.processing.configure(ADMIN,Processing.Mode.MANUAL,0);var lot=f.receive(token,key,PrazoHttpTest.PAYLOAD);f.processing.start(token,key);f.processing.advance(ADMIN,0,1);
        var jdbc=new JdbcTemplate(f.source);var inspection=new ScenarioService(new JdbcScenarioRepository(f.source),"cenario-a",ADMIN,f.auth);
        var guarded=new ProcessingService(f.auth,inspection,f.remessas,new JdbcRemessaRepository(f.source),new JdbcProcessingRepository(f.source),new JdbcTransactions(f.source),f.clock,f.clock,received->{
            var records=new StructuralRecords().process(received);jdbc.update("UPDATE remessas SET finalizada_em=? WHERE chave=?",INITIAL.toString(),key);return records;
        },"cenario-a");
        assertEquals(0,guarded.advance(ADMIN,0,1).passosExecutados());var before=snapshot(jdbc);assertEquals(0,guarded.advance(ADMIN,0,10).passosExecutados());guarded.consult(token,lot.number());assertEquals(before,snapshot(jdbc));assertTrue(guarded.inspect(ADMIN,lot.number()).effective().isEmpty());
        var repository=new JdbcRemessaRepository(f.source);var closed=repository.find(key,"cenario-a","0001").orElseThrow();
        assertNotNull(closed.finalizedAt());assertTrue(new JdbcProcessingRepository(f.source).eligible(closed).isEmpty());
        assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->repository.receive(closed,new LotePayload().read(RemessaHttpTest.PAYLOAD.getBytes(StandardCharsets.UTF_8)),"hash",INITIAL)).kind());
    }
    static Map<String,List<Map<String,Object>>> snapshot(JdbcTemplate jdbc) {
        var result=new TreeMap<String,List<Map<String,Object>>>();
        for(String table:jdbc.queryForList("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",String.class)) {
            var rows=jdbc.queryForList("SELECT * FROM "+table+" ORDER BY rowid");for(var row:rows) row.replaceAll((name,value)->value instanceof byte[] bytes?HexFormat.of().formatHex(bytes):value);result.put(table,rows);
        }
        result.put("sqlite_sequence",jdbc.queryForList("SELECT * FROM sqlite_sequence ORDER BY name"));return result;
    }
    static void removeExpirationMigration(Map<String,List<Map<String,Object>>> migrated) {
        assertEquals(14,((Number)migrated.get("schema_migrations").removeLast().get("version")).intValue());
        for(var row:migrated.get("remessas")) {
            var due=Remessa.deadline(Instant.parse((String)row.get("atividade")));
            assertEquals(due.getEpochSecond(),((Number)row.remove("expira_segundos")).longValue());
            assertEquals(due.getNano(),((Number)row.remove("expira_nanos")).intValue());assertNull(row.remove("expirada_em"));
        }
    }
    DataSource legacyEight() throws Exception {
        var helper=new JdbcProcessingTest();helper.temporary=temporary;var old=helper.legacySeven();var source=old.source();var jdbc=new JdbcTemplate(source);var resource=new ClassPathResource("db/migration/V8__processamento.sql");
        try(var c=source.getConnection()){ScriptUtils.executeSqlScript(c,resource);}jdbc.update("INSERT INTO schema_migrations VALUES(8,?)",CredentialHash.token(resource.getContentAsString(StandardCharsets.UTF_8)));
        jdbc.update("UPDATE processamento_config SET modo='AUTOMATICO',latencia_ms=17");jdbc.update("UPDATE relogio_negocio SET deslocamento_ms=123");
        for(int i=0;i<old.lots().size();i++) {
            var lot=old.lots().get(i);String state=switch(i%3){case 0->"PROCESSADO_SUCESSO";case 1->"EM_PROCESSAMENTO";default->"RECEBIDO";};
            jdbc.update("INSERT INTO processamento_trabalhos(lote_id,remessa_id,cenario,ug,geracao,situacao,devido_segundos,devido_nanos,latencia_ms) VALUES(?,?,?,?,0,?,?,?,17)",lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),state,INITIAL.getEpochSecond(),123);
            if(i%3==0) for(var record:new StructuralRecords().process(lot)) {
                jdbc.update("INSERT INTO registros_processados VALUES(?,?,?,?,0,?)",lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),record);
                jdbc.update("INSERT INTO registros_efetivos VALUES(?,?,?,?,0,?)",lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),record);
            }
        }
        for(String table:List.of("remessas","lotes","remessa_eventos","processamento_trabalhos")) jdbc.update("UPDATE sqlite_sequence SET seq=77 WHERE name=?",table);
        return source;
    }
    @Test void migraV8PreenchidaPreservaTodasAsTabelasChecksumsBytesESequencias() throws Exception {
        var source=legacyEight();var jdbc=new JdbcTemplate(source);var before=snapshot(jdbc);var f=fixture("cenario-a");assertEquals(14,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        var after=snapshot(jdbc);JdbcFinalizationTest.removeExpirationMigration(after);assertTrue(after.remove("competencias_bloqueadas").isEmpty());after.get("schema_migrations").removeLast();for(var row:after.get("remessas")){assertNull(row.remove("finalizada_em"));assertNull(row.remove("cancelada_em"));}after.get("schema_migrations").removeLast();after.get("schema_migrations").removeLast();after.get("schema_migrations").removeLast();after.remove("falhas_processamento");after.remove("falhas_recepcao");after.get("schema_migrations").removeLast();for(var row:after.get("lotes")) assertEquals(1,row.remove("ativo"));for(var row:after.get("processamento_trabalhos")) {assertNull(row.remove("codigo"));assertNull(row.remove("mensagem"));assertNull(row.remove("registro"));}assertEquals(before,after);
        var repository=new JdbcRemessaRepository(f.source);var remessa=repository.find(jdbc.queryForObject("SELECT chave FROM remessas",String.class),"cenario-a","0001").orElseThrow();assertNull(remessa.finalizedAt());
        assertEquals(Processing.Mode.AUTOMATICO,f.processing.configuration(ADMIN).modo());assertEquals(17,f.processing.configuration(ADMIN).latenciaMilissegundos());assertEquals(INITIAL.plusMillis(123),f.clock.instant());
        String token=f.token("0001"),key=remessa.key();assertEquals(RemessaFailure.Kind.CONFLICT,assertThrows(RemessaFailure.class,()->f.remessas.finish(token,key,totals(1))).kind());
        f.processing.advance(ADMIN,1000,100);var totals=new EnumMap<Assunto,java.math.BigInteger>(Assunto.class);for(var subject:Assunto.values()) totals.put(subject,java.math.BigInteger.ONE);f.remessas.finish(token,key,new Finalization(totals));
        var reopened=fixture("cenario-a");var next=reopened.remessas.open(token,202601,"NAO");assertEquals(78,next.id());assertTrue(reopened.receive(token,next.key(),PrazoHttpTest.PAYLOAD).number()>77);assertTrue(jdbc.queryForList("PRAGMA foreign_key_check").isEmpty());
    }
    @Test void falhaIntermediariaV9EChecksumIncompativelPreservamV8() throws Exception {
        var source=legacyEight();var jdbc=new JdbcTemplate(source);jdbc.execute("DROP INDEX remessa_aberta_unica");var before=snapshot(jdbc);
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));assertEquals(before,snapshot(jdbc));assertEquals(8,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        assertFalse(jdbc.queryForList("PRAGMA table_info(remessas)").stream().anyMatch(row->row.get("name").equals("finalizada_em")));
        jdbc.execute("CREATE UNIQUE INDEX remessa_aberta_unica ON remessas(cenario,ug,competencia) WHERE estado='ABERTO'");
        String checksum=jdbc.queryForObject("SELECT checksum FROM schema_migrations WHERE version=8",String.class);jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=8");var corrupt=snapshot(jdbc);
        assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));assertEquals(corrupt,snapshot(jdbc));assertEquals(8,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        jdbc.update("UPDATE schema_migrations SET checksum=? WHERE version=8",checksum);fixture("cenario-a");assertEquals(14,jdbc.queryForObject("PRAGMA user_version",Integer.class));
        jdbc.update("UPDATE schema_migrations SET checksum='adulterado' WHERE version=9");var v9=snapshot(jdbc);assertThrows(IllegalStateException.class,()->SqliteDatabase.open(settings("cenario-a")));assertEquals(v9,snapshot(jdbc));
    }
}
