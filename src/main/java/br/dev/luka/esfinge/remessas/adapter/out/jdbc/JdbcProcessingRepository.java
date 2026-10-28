package br.dev.luka.esfinge.remessas.adapter.out.jdbc;

import br.dev.luka.esfinge.remessas.application.port.out.ProcessingRepository;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Instant;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcProcessingRepository implements ProcessingRepository, br.dev.luka.esfinge.remessas.application.port.out.InspectionProcessing {
    private final JdbcTemplate jdbc;
    public JdbcProcessingRepository(DataSource source) {jdbc=new JdbcTemplate(source);}
    public Processing.Configuration configuration(String scenario,Instant now) {
        return jdbc.queryForObject("SELECT modo,latencia_ms FROM processamento_config WHERE cenario=?",(r,n)->new Processing.Configuration(Processing.Mode.valueOf(r.getString(1)),r.getLong(2),now),scenario);
    }
    public void configure(String scenario,Processing.Mode mode,long latencyMillis) {jdbc.update("UPDATE processamento_config SET modo=?,latencia_ms=? WHERE cenario=?",mode.name(),latencyMillis,scenario);}
    public long generation(String scenario) {return jdbc.queryForObject("SELECT generation FROM scenarios WHERE id=?",Long.class,scenario);}
    public boolean active(long remessaId,long generation) {return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 WHERE t.remessa_id=? AND t.geracao=? AND t.situacao IN ('RECEBIDO','EM_PROCESSAMENTO')",Integer.class,remessaId,generation)>0;}
    public List<Long> eligible(Remessa remessa) {
        return jdbc.queryForList("SELECT l.numero FROM lotes l JOIN remessas r ON r.id=l.remessa_id AND r.cenario=l.cenario AND r.ug=l.ug WHERE r.finalizada_em IS NULL AND r.cancelada_em IS NULL AND r.expirada_em IS NULL AND r.estado='ABERTO' AND l.ativo=1 AND l.remessa_id=? AND l.cenario=? AND l.ug=? AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero) ORDER BY l.ordem",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
    }
    public void schedule(Remessa remessa,List<Long> lots,long generation,Instant now,long latencyMillis) {
        remessa.requireOpen(now);
        if(JdbcRemessaRepository.renew(jdbc,remessa.id(),now)!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        var times=new ArrayList<Instant>();for(int i=0;i<lots.size();i++) times.add(now.plusMillis(Math.multiplyExact((long)i,latencyMillis)));
        for(int i=0;i<lots.size();i++) jdbc.update("INSERT INTO processamento_trabalhos(lote_id,remessa_id,cenario,ug,geracao,situacao,devido_segundos,devido_nanos,latencia_ms) VALUES (?,?,?,?,?,'RECEBIDO',?,?,?)",
            lots.get(i),remessa.id(),remessa.scenario(),remessa.ug(),generation,times.get(i).getEpochSecond(),times.get(i).getNano(),latencyMillis);
        event(remessa.id(),remessa.scenario(),remessa.ug(),null,"PROCESSAMENTO_DISPARADO",now);
    }
    public Optional<Processing.Work> next(String scenario,Instant now) {
        return jdbc.query("SELECT t.* FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.cenario=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND r.cancelada_em IS NULL AND r.expirada_em IS NULL AND (r.expira_segundos>? OR (r.expira_segundos=? AND r.expira_nanos>?)) AND t.situacao IN ('RECEBIDO','EM_PROCESSAMENTO') AND (t.devido_segundos<? OR (t.devido_segundos=? AND t.devido_nanos<=?)) ORDER BY t.devido_segundos,t.devido_nanos,t.id LIMIT 1",
            (r,n)->new Processing.Work(r.getLong("id"),r.getLong("lote_id"),r.getLong("remessa_id"),r.getString("cenario"),r.getString("ug"),r.getLong("geracao"),Processing.State.valueOf(r.getString("situacao")),Instant.ofEpochSecond(r.getLong("devido_segundos"),r.getInt("devido_nanos")),r.getLong("latencia_ms")),scenario,now.getEpochSecond(),now.getEpochSecond(),now.getNano(),now.getEpochSecond(),now.getEpochSecond(),now.getNano()).stream().findFirst();
    }
    private boolean current(Processing.Work work,Instant now) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM processamento_trabalhos t JOIN lotes l ON l.numero=t.lote_id AND l.ativo=1 JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao JOIN remessas r ON r.id=t.remessa_id AND r.cenario=t.cenario AND r.ug=t.ug WHERE t.id=? AND t.lote_id=? AND t.cenario=? AND t.ug=? AND t.geracao=? AND t.situacao=? AND t.devido_segundos=? AND t.devido_nanos=? AND t.latencia_ms=? AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND r.cancelada_em IS NULL AND r.expirada_em IS NULL AND (r.expira_segundos>? OR (r.expira_segundos=? AND r.expira_nanos>?))",Integer.class,work.id(),work.loteId(),work.cenario(),work.ug(),work.geracao(),work.state().name(),work.due().getEpochSecond(),work.due().getNano(),work.latencyMillis(),now.getEpochSecond(),now.getEpochSecond(),now.getNano())==1;
    }
    public boolean begin(Processing.Work work,Instant due,Instant now) {
        if(work.state()!=Processing.State.RECEBIDO || !current(work,now)) return false;
        jdbc.update("UPDATE processamento_trabalhos SET situacao='EM_PROCESSAMENTO',devido_segundos=?,devido_nanos=? WHERE id=?",due.getEpochSecond(),due.getNano(),work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_EM_PROCESSAMENTO",work.due());return true;
    }
    public boolean complete(Processing.Work work,List<byte[]> records,Instant now) {
        if(work.state()!=Processing.State.EM_PROCESSAMENTO || now.isBefore(work.due()) || !current(work,now)) return false;
        for(int i=0;i<records.size();i++) {
            jdbc.update("INSERT INTO registros_processados(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
            jdbc.update("INSERT INTO registros_efetivos(lote_id,remessa_id,cenario,ug,indice,payload) VALUES (?,?,?,?,?,?)",work.loteId(),work.remessaId(),work.cenario(),work.ug(),i,records.get(i));
        }
        jdbc.update("UPDATE processamento_trabalhos SET situacao='PROCESSADO_SUCESSO' WHERE id=?",work.id());
        event(work.remessaId(),work.cenario(),work.ug(),work.loteId(),"LOTE_PROCESSADO_SUCESSO",work.due());return true;
    }
    public void discard(Remessa remessa,Assunto subject,long generation,Instant now) {
        var selected=jdbc.queryForList("SELECT numero FROM lotes WHERE remessa_id=? AND cenario=? AND ug=? AND assunto=? AND ativo=1 ORDER BY ordem",Long.class,remessa.id(),remessa.scenario(),remessa.ug(),subject.name());
        for(long lot:selected) {
            if(state(lot)==Processing.State.RECEBIDO) {
                var work=new Processing.Work(0,lot,remessa.id(),remessa.scenario(),remessa.ug(),generation,Processing.State.RECEBIDO,now,0);
                terminal(work,lot,Processing.State.LOTE_DESCARTADO,"SIM_DESCARTE_ASSUNTO","Dados removidos por descarte do assunto",null,now);
            } else {
                removeData(lot);
                event(remessa.id(),remessa.scenario(),remessa.ug(),lot,"DADOS_LOTE_REMOVIDOS",now);
            }
        }
        jdbc.update("UPDATE falhas_recepcao SET recuperacao=0 WHERE remessa_id=? AND cenario=? AND ug=? AND assunto=? AND geracao=?",remessa.id(),remessa.scenario(),remessa.ug(),subject.name(),generation);
        if(JdbcRemessaRepository.renew(jdbc,remessa.id(),now)!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        event(remessa.id(),remessa.scenario(),remessa.ug(),null,"ASSUNTO_DESCARTADO_"+subject.name(),now);
    }
    public void cancel(Remessa remessa,long generation,Instant now) {
        remessa.requireOpen(now);var due=Remessa.deadline(now);
        if(jdbc.update("UPDATE remessas SET cancelada_em=?,atividade=?,expira_segundos=?,expira_nanos=? WHERE id=? AND cenario=? AND ug=? AND estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL AND expirada_em IS NULL AND (expira_segundos>? OR (expira_segundos=? AND expira_nanos>?))",now.toString(),now.toString(),due.getEpochSecond(),due.getNano(),remessa.id(),remessa.scenario(),remessa.ug(),now.getEpochSecond(),now.getEpochSecond(),now.getNano())!=1)
            throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        removeAll(remessa,generation,now,"SIM_ENVIO_CANCELADO","Envio cancelado no simulador");
        event(remessa.id(),remessa.scenario(),remessa.ug(),null,"ENVIO_CANCELADO",now);
    }
    public boolean expire(Remessa remessa,long generation,Instant now) {
        if(jdbc.update("UPDATE remessas SET expirada_em=? WHERE id=? AND cenario=? AND ug=? AND estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL AND expirada_em IS NULL AND (expira_segundos<? OR (expira_segundos=? AND expira_nanos<=?)) AND EXISTS(SELECT 1 FROM scenarios WHERE id=remessas.cenario AND generation=?)",now.toString(),remessa.id(),remessa.scenario(),remessa.ug(),now.getEpochSecond(),now.getEpochSecond(),now.getNano(),generation)==0) return false;
        removeAll(remessa,generation,now,"SIM_ENVIO_EXPIRADO","Envio expirado no simulador");
        event(remessa.id(),remessa.scenario(),remessa.ug(),null,"ENVIO_EXPIRADO",now);return true;
    }
    public Processing.ResetCounts reset(String scenario) {
        for(String table:List.of("registros_efetivos","registros_processados","valores_recebidos","falhas_processamento","processamento_trabalhos","falhas_recepcao","remessa_eventos"))
            jdbc.update("DELETE FROM "+table+" WHERE cenario=?",scenario);
        long lots=jdbc.update("DELETE FROM lotes WHERE cenario=?",scenario);
        long remittances=jdbc.update("DELETE FROM remessas WHERE cenario=?",scenario);
        jdbc.update("DELETE FROM competencias_bloqueadas WHERE cenario=?",scenario);
        if(jdbc.update("UPDATE processamento_config SET modo='MANUAL',latencia_ms=1000 WHERE cenario=?",scenario)!=1
            || jdbc.update("UPDATE relogio_negocio SET deslocamento_ms=0 WHERE cenario=?",scenario)!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        return new Processing.ResetCounts(remittances,lots);
    }
    private void removeAll(Remessa remessa,long generation,Instant now,String code,String message) {
        var selected=jdbc.queryForList("SELECT numero FROM lotes WHERE remessa_id=? AND cenario=? AND ug=? AND ativo=1 ORDER BY ordem",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
        for(long lot:selected) {
            var state=state(lot);
            if(state==Processing.State.RECEBIDO || state==Processing.State.EM_PROCESSAMENTO) {
                var work=new Processing.Work(0,lot,remessa.id(),remessa.scenario(),remessa.ug(),generation,state,now,0);
                terminal(work,lot,Processing.State.ABORTADO,code,message,null,now);
            } else {
                removeData(lot);
                event(remessa.id(),remessa.scenario(),remessa.ug(),lot,"DADOS_LOTE_REMOVIDOS",now);
            }
        }
        jdbc.update("UPDATE falhas_recepcao SET recuperacao=0 WHERE remessa_id=? AND cenario=? AND ug=? AND geracao=?",remessa.id(),remessa.scenario(),remessa.ug(),generation);
    }
    public boolean isActive(long lot) {return jdbc.queryForObject("SELECT ativo FROM lotes WHERE numero=?",Integer.class,lot)==1;}
    public Processing.Diagnostic diagnostic(long lot) {
        return jdbc.query("SELECT codigo,mensagem,registro FROM processamento_trabalhos WHERE lote_id=? AND codigo IS NOT NULL",(r,n)->new Processing.Diagnostic(r.getString(1),r.getString(2),(Integer)r.getObject(3)),lot).stream().findFirst().orElse(null);
    }
    public Optional<Processing.FailurePlan> failurePlan(long lot,String scenario,long generation) {
        return jdbc.query("SELECT registro FROM falhas_processamento WHERE lote_id=? AND cenario=? AND geracao=?",(r,n)->new Processing.FailurePlan(lot,(Integer)r.getObject(1)),lot,scenario,generation).stream().findFirst();
    }
    private void requireConfigurable(Lote lot,long generation,Instant now) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM lotes l JOIN remessas r ON r.id=l.remessa_id JOIN scenarios s ON s.id=l.cenario WHERE l.numero=? AND l.ativo=1 AND r.estado='ABERTO' AND r.finalizada_em IS NULL AND r.cancelada_em IS NULL AND r.expirada_em IS NULL AND (r.expira_segundos>? OR (r.expira_segundos=? AND r.expira_nanos>?)) AND s.generation=? AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero AND t.situacao!='RECEBIDO')",Integer.class,lot.number(),now.getEpochSecond(),now.getEpochSecond(),now.getNano(),generation)!=1) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
    }
    public void planFailure(Lote lot,long generation,Integer record,Instant now) {
        requireConfigurable(lot,generation,now);
        jdbc.update("INSERT INTO falhas_processamento(lote_id,remessa_id,cenario,ug,geracao,registro) VALUES(?,?,?,?,?,?) ON CONFLICT(lote_id) DO UPDATE SET geracao=excluded.geracao,registro=excluded.registro",lot.number(),lot.remessaId(),lot.scenario(),lot.ug(),generation,record);
        event(lot.remessaId(),lot.scenario(),lot.ug(),lot.number(),"FALHA_PROCESSAMENTO_CONFIGURADA",now);
    }
    public void removeFailure(Lote lot,long generation,Instant now) {
        requireConfigurable(lot,generation,now);
        if(jdbc.update("DELETE FROM falhas_processamento WHERE lote_id=? AND geracao=?",lot.number(),generation)!=1) throw new RemessaFailure(RemessaFailure.Kind.NOT_FOUND);
        event(lot.remessaId(),lot.scenario(),lot.ug(),lot.number(),"FALHA_PROCESSAMENTO_REMOVIDA",now);
    }
    public boolean fail(Processing.Work work,Instant now) {
        if(work.state()!=Processing.State.EM_PROCESSAMENTO || now.isBefore(work.due()) || !current(work,now)) return false;
        var plan=failurePlan(work.loteId(),work.cenario(),work.geracao());if(plan.isEmpty()) return false;
        var following=jdbc.queryForList("SELECT l.numero FROM lotes l JOIN lotes alvo ON alvo.numero=? WHERE l.remessa_id=alvo.remessa_id AND l.cenario=alvo.cenario AND l.ug=alvo.ug AND l.assunto=alvo.assunto AND l.ordem>alvo.ordem AND l.ativo=1 AND NOT EXISTS(SELECT 1 FROM processamento_trabalhos t WHERE t.lote_id=l.numero AND t.situacao NOT IN ('RECEBIDO','EM_PROCESSAMENTO')) ORDER BY l.ordem",Long.class,work.loteId());
        terminal(work,work.loteId(),Processing.State.LOTE_DESCARTADO,"SIM_ERRO_INJETADO","Falha de processamento configurada no simulador",plan.get().registro(),now);
        for(long lot:following) terminal(work,lot,Processing.State.ABORTADO_ERRO_LOTE_ANTERIOR,"SIM_ABORTADO_ERRO_LOTE_ANTERIOR","Lote abortado por erro em lote anterior do mesmo assunto",null,now);
        return true;
    }
    private void terminal(Processing.Work work,long lot,Processing.State state,String code,String message,Integer record,Instant now) {
        removeData(lot);
        jdbc.update("INSERT INTO processamento_trabalhos(lote_id,remessa_id,cenario,ug,geracao,situacao,devido_segundos,devido_nanos,latencia_ms,codigo,mensagem,registro) VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(lote_id) DO UPDATE SET situacao=excluded.situacao,codigo=excluded.codigo,mensagem=excluded.mensagem,registro=excluded.registro",lot,work.remessaId(),work.cenario(),work.ug(),work.geracao(),state.name(),now.getEpochSecond(),now.getNano(),work.latencyMillis(),code,message,record);
        event(work.remessaId(),work.cenario(),work.ug(),lot,state.name(),now);
    }
    private void removeData(long lot) {
        jdbc.update("DELETE FROM registros_efetivos WHERE lote_id=?",lot);
        jdbc.update("DELETE FROM registros_processados WHERE lote_id=?",lot);
        jdbc.update("DELETE FROM valores_recebidos WHERE lote_id=?",lot);
        jdbc.update("UPDATE lotes SET ativo=0 WHERE numero=?",lot);
    }
    private void event(long remessa,String scenario,String ug,Long lot,String type,Instant now) {jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa,scenario,ug,lot,type,now.toString());}
    public Processing.State state(long lot) {return jdbc.query("SELECT situacao FROM processamento_trabalhos WHERE lote_id=?",(r,n)->Processing.State.valueOf(r.getString(1)),lot).stream().findFirst().orElse(Processing.State.RECEBIDO);}
    public List<byte[]> processed(long lot) {return jdbc.query("SELECT payload FROM registros_processados WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}
    public List<byte[]> effective(long lot) {return jdbc.query("SELECT payload FROM registros_efetivos WHERE lote_id=? ORDER BY indice",(r,n)->r.getBytes(1),lot);}

    public Inspection.State summary(String scenario,long lot) {
        return new Inspection.State(state(lot),diagnostic(lot),jdbc.queryForObject("SELECT COUNT(*) FROM registros_processados WHERE cenario=? AND lote_id=?",Long.class,scenario,lot),jdbc.queryForObject("SELECT COUNT(*) FROM registros_efetivos WHERE cenario=? AND lote_id=?",Long.class,scenario,lot));
    }
    public List<Inspection.Record> records(String scenario,long lot,Inspection.Type type,Inspection.Window w) {
        String table=switch(type){case PROCESSADOS->"registros_processados";case EFETIVOS->"registros_efetivos";default->throw new IllegalArgumentException();};
        return jdbc.query("SELECT indice,payload FROM "+table+" WHERE cenario=? AND lote_id=? AND indice>? AND indice<=? ORDER BY indice LIMIT ?",(r,n)->new Inspection.Record(r.getLong(1),r.getBytes(2)),scenario,lot,w.depois(),w.teto(),w.limite());
    }
    public List<Inspection.ResultCounts> counts(String scenario,Long remessa) {
        return jdbc.query("SELECT l.assunto,COALESCE(t.situacao,'RECEBIDO'),COUNT(*),COALESCE(SUM((SELECT COUNT(*) FROM registros_processados p WHERE p.lote_id=l.numero)),0),COALESCE(SUM((SELECT COUNT(*) FROM registros_efetivos e WHERE e.lote_id=l.numero)),0) FROM lotes l LEFT JOIN processamento_trabalhos t ON t.lote_id=l.numero WHERE l.cenario=?"+(remessa==null?"":" AND l.remessa_id=?")+" GROUP BY l.assunto,COALESCE(t.situacao,'RECEBIDO') ORDER BY l.assunto,2",
            (r,n)->new Inspection.ResultCounts(Assunto.valueOf(r.getString(1)),r.getString(2),r.getLong(3),r.getLong(4),r.getLong(5)),remessa==null?new Object[]{scenario}:new Object[]{scenario,remessa});
    }
}
