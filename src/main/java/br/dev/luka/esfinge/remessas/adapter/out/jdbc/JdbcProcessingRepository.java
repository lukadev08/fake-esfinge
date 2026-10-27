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
    public boolean active(long remessaId,long generation) {return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 WHERE t.remessa_id=? AND t.geracao=? AND t.situacao IN ('RECEBIDO','EM_PROCESSAMENTO')",Integer.class,remessaId,generation)>0;}
    public List<Long> eligible(Remessa remessa) {
        return jdbc.queryForList("SELECT l.numero FROM lotes l JOIN remessas r ON r.id=l.remessa_id AND r.cenario=l.cenario AND r.ug=l.ug WHERE r.finalizada_em IS NULL AND r.estado='ABERTO' AND l.ativo=1 AND l.remessa_id=? AND l.cenario=? AND l.ug=? AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero) ORDER BY l.ordem",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
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
        return jdbc.query("SELECT t.* FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.cenario=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND t.situacao IN ('RECEBIDO','EM_PROCESSAMENTO') AND (t.devido_segundos<? OR (t.devido_segundos=? AND t.devido_nanos<=?)) ORDER BY t.devido_segundos,t.devido_nanos,t.id LIMIT 1",
            (r,n)->new Processing.Work(r.getLong("id"),r.getLong("lote_id"),r.getLong("remessa_id"),r.getString("cenario"),r.getString("ug"),r.getLong("geracao"),Processing.State.valueOf(r.getString("situacao")),Instant.ofEpochSecond(r.getLong("devido_segundos"),r.getInt("devido_nanos")),r.getLong("latencia_ms")),scenario,now.getEpochSecond(),now.getEpochSecond(),now.getNano()).stream().findFirst();
    }
    private boolean current(Processing.Work work) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.id=? AND t.lote_id=? AND t.cenario=? AND t.ug=? AND t.geracao=? AND t.situacao=? AND t.devido_segundos=? AND t.devido_nanos=? AND t.latencia_ms=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL",Integer.class,work.id(),work.loteId(),work.cenario(),work.ug(),work.geracao(),work.state().name(),work.due().getEpochSecond(),work.due().getNano(),work.latencyMillis())==1;
    }
    public boolean begin(Processing.Work work,Instant due) {
        if(work.state()!=Processing.State.RECEBIDO || !current(work)) return false;
        jdbc.update("UPDATE processamento_trabalhos SET situacao='EM_PROCESSAMENTO',devido_segundos=?,devido_nanos=? WHERE id=?",due.getEpochSecond(),due.getNano(),work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_EM_PROCESSAMENTO",work.due());return true;
    }
    public boolean complete(Processing.Work work,List<byte[]> records,Instant now) {
        if(work.state()!=Processing.State.EM_PROCESSAMENTO || now.isBefore(work.due()) || !current(work)) return false;
        for(int i=0;i<records.size();i++) {
            jdbc.update("INSERT INTO registros_processados(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
            jdbc.update("INSERT INTO registros_efetivos(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
        }
        jdbc.update("UPDATE processamento_trabalhos SET situacao='PROCESSADO_SUCESSO' WHERE id=?",work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_PROCESSADO_SUCESSO",work.due());return true;
    }
    public boolean isActive(long lot) {return jdbc.queryForObject("SELECT ativo FROM lotes WHERE numero=?",Integer.class,lot)==1;}
    public Processing.Diagnostic diagnostic(long lot) {
        return jdbc.query("SELECT codigo,mensagem,registro FROM processamento_trabalhos WHERE lote_id=? AND codigo IS NOT NULL",(r,n)->new Processing.Diagnostic(r.getString(1),r.getString(2),(Integer)r.getObject(3)),lot).stream().findFirst().orElse(null);
    }
    public Optional<Processing.FailurePlan> failurePlan(long lot,String scenario,long generation) {
        return jdbc.query("SELECT registro FROM falhas_processamento WHERE lote_id=? AND cenario=? AND geracao=?",(r,n)->new Processing.FailurePlan(lot,(Integer)r.getObject(1)),lot,scenario,generation).stream().findFirst();
    }
    private void requireConfigurable(Lote lot,long generation) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM lotes l JOIN remessas r ON r.id=l.remessa_id JOIN scenarios s ON s.id=l.cenario WHERE l.numero=? AND l.ativo=1 AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND s.generation=? AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero AND t.situacao!='RECEBIDO')",Integer.class,lot.number(),generation)!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
    }
    public void planFailure(Lote lot,long generation,Integer record,Instant now) {
        requireConfigurable(lot,generation);
        jdbc.update("INSERT INTO falhas_processamento(lote_id,remessa_id,cenario,ug,geracao,registro) VALUES(?,?,?,?,?,?) ON CONFLICT(lote_id) DO UPDATE SET geracao=excluded.geracao,registro=excluded.registro",lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),generation,record);
        event(lot.remessaId(),lot.scenario(),lot.ug(),lot.number(),"FALHA_PROCESSAMENTO_CONFIGURADA",now);
    }
    public void removeFailure(Lote lot,long generation,Instant now) {
        requireConfigurable(lot,generation);
        if(jdbc.update("DELETE FROM falhas_processamento WHERE lote_id=? AND geracao=?",lot.number(),generation)!=1) throw new RemessaFailure(RemessaFailure.Kind.NOT_FOUND);
        event(lot.remessaId(),lot.scenario(),lot.ug(),lot.number(),"FALHA_PROCESSAMENTO_REMOVIDA",now);
    }
    public boolean fail(Processing.Work work,Instant now) {
        if(work.state()!=Processing.State.EM_PROCESSAMENTO || now.isBefore(work.due()) || !current(work)) return false;
        var plan=failurePlan(work.loteId(),work.cenario(),work.geracao());if(plan.isEmpty()) return false;
        var following=jdbc.queryForList("SELECT l.numero FROM lotes l JOIN lotes alvo ON alvo.numero=? WHERE l.remessa_id=alvo.remessa_id AND l.cenario=alvo.cenario AND l.ug=alvo.ug AND l.assunto=alvo.assunto AND l.ordem>alvo.ordem AND l.ativo=1 AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero AND t.situacao NOT IN ('RECEBIDO','EM_PROCESSAMENTO')) ORDER BY l.ordem",Long.class,work.loteId());
        terminal(work,work.loteId(),Processing.State.LOTE_DESCARTADO,"SIM_ERRO_INJETADO","Falha de processamento configurada no simulador",plan.get().registro(),now);
        for(long lot:following) terminal(work,lot,Processing.State.ABORTADO_ERRO_LOTE_ANTERIOR,"SIM_ABORTADO_ERRO_LOTE_ANTERIOR","Lote abortado por erro em lote anterior do mesmo assunto",null,now);
        return true;
    }
    private void terminal(Processing.Work work,long lot,Processing.State state,String code,String message,Integer record,Instant now) {
        jdbc.update("DELETE FROM registros_efetivos WHERE lote_id=?",lot);
        jdbc.update("DELETE FROM registros_processados WHERE lote_id=?",lot);
        jdbc.update("DELETE FROM valores_recebidos WHERE lote_id=?",lot);
        jdbc.update("UPDATE lotes SET ativo=0 WHERE numero=?",lot);
        jdbc.update("INSERT INTO processamento_trabalhos(lote_id,remessa_id,cenario,ug,geracao,situacao,devido_segundos,devido_nanos,latencia_ms,codigo,mensagem,registro) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(lote_id) DO UPDATE SET situacao=excluded.situacao,codigo=excluded.codigo,mensagem=excluded.mensagem,registro=excluded.registro",lot,work.remessaId(),work.cenario(),work.ug(),work.geracao(),state.name(),now.getEpochSecond(),now.getNano(),work.latencyMillis(),code,message,record);
        event(work.remessaId(),work.cenario(),work.ug(),lot,state.name(),now);
    }
    private void event(long remessa,String scenario,String ug,Long lot,String type,Instant now) {jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa,scenario,ug,lot,type,now.toString());}
    public Processing.State state(long lot) {return jdbc.query("SELECT situacao FROM processamento_trabalhos WHERE lote_id=?",(r,n)->Processing.State.valueOf(r.getString(1)),lot).stream().findFirst().orElse(Processing.State.RECEBIDO);}
    public List<byte[]> processed(long lot) {return jdbc.query("SELECT payload FROM registros_processados WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}
    public List<byte[]> effective(long lot) {return jdbc.query("SELECT payload FROM registros_efetivos WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}
}
