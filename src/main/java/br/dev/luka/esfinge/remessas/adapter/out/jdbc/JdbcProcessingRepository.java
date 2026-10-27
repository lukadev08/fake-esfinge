package br.dev.luka.esfinge.remessas.adapter.out.jdbc;

import br.dev.luka.esfinge.remessas.application.port.out.ProcessingRepository;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcProcessingRepository implements ProcessingRepository {
    private final JdbcTemplate jdbc;
    public JdbcProcessingRepository(DataSource source) {jdbc=new JdbcTemplate(source);}
    public Processing.Configuration configuration(String scenario,Instant now) {
        return jdbc.queryForObject("SELECT modo,latencia_ms FROM processamento_config WHERE cenario=?",(r,n)->new Processing.Configuration(Processing.Mode.valueOf(r.getString(1)),r.getLong(2),now),scenario);
    }
    public void configure(String scenario,Processing.Mode mode,long latencyMillis) {jdbc.update("UPDATE processamento_config SET modo=?,latencia_ms=? WHERE cenario=?",mode.name(),latencyMillis,scenario);}
    public long generation(String scenario) {return jdbc.queryForObject("SELECT generation FROM scenarios WHERE id=?",Long.class,scenario);}
    public boolean active(long remessaId,long generation) {return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos WHERE remessa_id=? AND geracao=? AND situacao!='PROCESSADO_SUCESSO'",Integer.class,remessaId,generation)>0;}
    public List<Long> eligible(Remessa remessa) {
        return jdbc.queryForList("SELECT l.numero FROM lotes l JOIN remessas r ON r.id=l.remessa_id AND r.cenario=l.cenario AND r.ug=l.ug WHERE r.finalizada_em IS NULL AND r.estado='ABERTO' AND l.remessa_id=? AND l.cenario=? AND l.ug=? AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero) ORDER BY l.ordem",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
    }
    public void schedule(Remessa remessa,List<Long> lots,long generation,Instant now,long latencyMillis) {
        remessa.requireOpen();
        if(jdbc.queryForObject("SELECT COUNT(*) FROM remessas WHERE id=? AND finalizada_em IS NULL AND estado='ABERTO'",Integer.class,remessa.id())!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        var times=new ArrayList<Instant>();for(int i=0;i<lots.size();i++) times.add(now.plusMillis(Math.multiplyExact((long)i,latencyMillis)));
        for(int i=0;i<lots.size();i++) jdbc.update("INSERT INTO processamento_trabalhos(lote_id,remessa_id,cenario,ug,geracao,situacao,devido_segundos,devido_nanos,latencia_ms) VALUES (?,?,?,?,?,'RECEBIDO',?,?,?)",
            lots.get(i),remessa.id(),remessa.scenario(),remessa.ug(),generation,times.get(i).getEpochSecond(),times.get(i).getNano(),latencyMillis);
        jdbc.update("UPDATE remessas SET atividade=? WHERE id=? AND finalizada_em IS NULL",now.toString(),remessa.id());
        event(remessa.id(),remessa.scenario(),remessa.ug(),null,"PROCESSAMENTO_DISPARADO",now);
    }
    public Optional<Processing.Work> next(String scenario,Instant now) {
        return jdbc.query("SELECT t.* FROM processamento_trabalhos t JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.cenario=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND t.situacao!='PROCESSADO_SUCESSO' AND (t.devido_segundos<? OR (t.devido_segundos=? AND t.devido_nanos<=?)) ORDER BY t.devido_segundos,t.devido_nanos,t.id LIMIT 1",
            (r,n)->new Processing.Work(r.getLong("id"),r.getLong("lote_id"),r.getLong("remessa_id"),r.getString("cenario"),r.getString("ug"),r.getLong("geracao"),Processing.State.valueOf(r.getString("situacao")),Instant.ofEpochSecond(r.getLong("devido_segundos"),r.getInt("devido_nanos")),r.getLong("latencia_ms")),scenario,now.getEpochSecond(),now.getEpochSecond(),now.getNano()).stream().findFirst();
    }
    private boolean current(Processing.Work work) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos t JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.id=? AND t.lote_id=? AND t.cenario=? AND t.ug=? AND t.geracao=? AND t.situacao=? AND t.devido_segundos=? AND t.devido_nanos=? AND t.latencia_ms=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL",Integer.class,work.id(),work.loteId(),work.cenario(),work.ug(),work.geracao(),work.state().name(),work.due().getEpochSecond(),work.due().getNano(),work.latencyMillis())==1;
    }
    public boolean begin(Processing.Work work,Instant due) {
        if(!current(work)) return false;
        jdbc.update("UPDATE processamento_trabalhos SET situacao='EM_PROCESSAMENTO',devido_segundos=?,devido_nanos=? WHERE id=?",due.getEpochSecond(),due.getNano(),work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_EM_PROCESSAMENTO",work.due());return true;
    }
    public boolean complete(Processing.Work work,List<byte[]> records,Instant now) {
        if(now.isBefore(work.due()) || !current(work)) return false;
        for(int i=0;i<records.size();i++) {
            jdbc.update("INSERT INTO registros_processados(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
            jdbc.update("INSERT INTO registros_efetivos(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
        }
        jdbc.update("UPDATE processamento_trabalhos SET situacao='PROCESSADO_SUCESSO' WHERE id=?",work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_PROCESSADO_SUCESSO",work.due());return true;
    }
    private void event(long remessa,String scenario,String ug,Long lot,String type,Instant now) {jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa,scenario,ug,lot,type,now.toString());}
    public Processing.State state(long lot) {return jdbc.query("SELECT situacao FROM processamento_trabalhos WHERE lote_id=?",(r,n)->Processing.State.valueOf(r.getString(1)),lot).stream().findFirst().orElse(Processing.State.RECEBIDO);}
    public List<byte[]> processed(long lot) {return jdbc.query("SELECT payload FROM registros_processados WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}
    public List<byte[]> effective(long lot) {return jdbc.query("SELECT payload FROM registros_efetivos WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}
}
