package br.dev.luka.esfinge.remessas.adapter.out.jdbc;

import br.dev.luka.esfinge.remessas.application.port.out.RemessaRepository;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

public final class JdbcRemessaRepository implements RemessaRepository {
    private final JdbcTemplate jdbc;
    public JdbcRemessaRepository(DataSource source) { jdbc = new JdbcTemplate(source); }
    public Remessa open(String scenario, String ug, int yearMonth, String key, Instant now) {
        long id;
        try {
            id = jdbc.queryForObject("INSERT INTO remessas(chave,cenario,ug,competencia,abertura,atividade) VALUES (?,?,?,?,?,?) RETURNING id", Long.class,
                    key, scenario, ug, yearMonth, now.toString(), now.toString());
        } catch (DataAccessException failure) {
            if (failure.getMostSpecificCause() instanceof org.sqlite.SQLiteException sqlite
                    && sqlite.getResultCode() == org.sqlite.SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE)
                throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
            throw failure;
        }
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,tipo,instante) VALUES (?,?,?,?,?)", id, scenario, ug, "ENVIO_ABERTO", now.toString());
        return new Remessa(id, key, scenario, ug, yearMonth, now, now, null, null);
    }
    public java.util.Optional<Remessa> find(String key,String scenario,String ug) {
        return jdbc.query("SELECT id,chave,cenario,ug,competencia,abertura,atividade,finalizada_em,cancelada_em FROM remessas WHERE chave=? AND cenario=? AND ug=?",
                (r,n) -> new Remessa(r.getLong("id"),r.getString("chave"),r.getString("cenario"),r.getString("ug"),r.getInt("competencia"),
                        Instant.parse(r.getString("abertura")),Instant.parse(r.getString("atividade")),r.getString("finalizada_em")==null?null:Instant.parse(r.getString("finalizada_em")),r.getString("cancelada_em")==null?null:Instant.parse(r.getString("cancelada_em"))),key,scenario,ug).stream().findFirst();
    }
    public java.util.Optional<Remessa> find(String key,String scenario) {
        var ugs=jdbc.queryForList("SELECT ug FROM remessas WHERE chave=? AND cenario=?",String.class,key,scenario);
        return ugs.isEmpty()?java.util.Optional.empty():find(key,scenario,ugs.getFirst());
    }
    private long generation(Remessa remessa) {return jdbc.queryForObject("SELECT generation FROM scenarios WHERE id=?",Long.class,remessa.scenario());}
    public java.util.Optional<Reception.Plan> receptionPlan(Remessa remessa,Assunto subject) {
        return jdbc.query("SELECT tipo,tentativa,tentativas,consumida,recuperacao FROM falhas_recepcao WHERE remessa_id=? AND cenario=? AND ug=? AND assunto=? AND geracao=?",
            (r,n)->new Reception.Plan(remessa.key(),subject,Reception.Type.valueOf(r.getString(1)),r.getLong(2),r.getLong(3),r.getBoolean(4),r.getBoolean(5)),remessa.id(),remessa.scenario(),remessa.ug(),subject.name(),generation(remessa)).stream().findFirst();
    }
    public void planReception(Remessa remessa,Assunto subject,Reception.Type type,long attempt,Instant now) {
        requireOpen(remessa.id());var existing=receptionPlan(remessa,subject);
        if(existing.isPresent() && existing.get().recuperacaoPendente()) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        jdbc.update("INSERT INTO falhas_recepcao(remessa_id,cenario,ug,geracao,assunto,tipo,tentativa) VALUES(?,?,?,?,?,?,?) ON CONFLICT(remessa_id,assunto,geracao) DO UPDATE SET tipo=excluded.tipo,tentativa=excluded.tentativa,tentativas=0,consumida=0,recuperacao=0",
            remessa.id(),remessa.scenario(),remessa.ug(),generation(remessa),subject.name(),type.name(),attempt);
        receptionEvent(remessa,null,"FALHA_RECEPCAO_CONFIGURADA",now);
    }
    public void removeReceptionPlan(Remessa remessa,Assunto subject,Instant now) {
        requireOpen(remessa.id());var plan=receptionPlan(remessa,subject).orElseThrow(()->new RemessaFailure(RemessaFailure.Kind.NOT_FOUND));
        if(plan.recuperacaoPendente()) throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        jdbc.update("DELETE FROM falhas_recepcao WHERE remessa_id=? AND assunto=? AND geracao=?",remessa.id(),subject.name(),generation(remessa));
        receptionEvent(remessa,null,"FALHA_RECEPCAO_REMOVIDA",now);
    }
    public Reception.Type receptionAttempt(Remessa remessa,Assunto subject) {
        var current=receptionPlan(remessa,subject);if(current.isEmpty()) return null;var plan=current.get();
        if(plan.recuperacaoPendente()) throw new RemessaFailure(RemessaFailure.Kind.RECOVERY_REQUIRED);
        if(plan.consumida()) return null;
        long attempt=plan.tentativas()+1;boolean selected=attempt==plan.tentativa();
        jdbc.update("UPDATE falhas_recepcao SET tentativas=?,consumida=? WHERE remessa_id=? AND assunto=? AND geracao=?",attempt,selected?1:0,remessa.id(),subject.name(),generation(remessa));
        return selected?plan.tipo():null;
    }
    public void responseLost(Remessa remessa,Lote lot,Instant now) {
        jdbc.update("UPDATE falhas_recepcao SET recuperacao=1 WHERE remessa_id=? AND assunto=? AND geracao=?",remessa.id(),lot.subject().name(),generation(remessa));
        receptionEvent(remessa,lot.number(),"RESPOSTA_LOTE_PERDIDA",now);
    }
    private void receptionEvent(Remessa remessa,Long lot,String type,Instant now) {
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES(?,?,?,?,?,?)",remessa.id(),remessa.scenario(),remessa.ug(),lot,type,now.toString());
    }
    public Lote receive(Remessa remessa,ReceivedContent content,String hash,Instant now) {
        remessa.requireOpen();
        requireOpen(remessa.id());
        int order=jdbc.queryForObject("SELECT COALESCE(MAX(ordem),0)+1 FROM lotes WHERE remessa_id=?",Integer.class,remessa.id());
        long number=jdbc.queryForObject("INSERT INTO lotes(remessa_id,cenario,ug,ordem,assunto,payload,hash,registros,recebimento) VALUES (?,?,?,?,?,?,?,?,?) RETURNING numero",Long.class,
                remessa.id(),remessa.scenario(),remessa.ug(),order,content.subject().name(),content.bytes(),hash,content.records(),now.toString());
        for (int i=0;i<content.monetaryValues().size();i++) for (var value:content.monetaryValues().get(i).entrySet()) jdbc.update(
                "INSERT INTO valores_recebidos(lote_id,remessa_id,cenario,ug,indice,campo,valor) VALUES (?,?,?,?,?,?,?)",
                number,remessa.id(),remessa.scenario(),remessa.ug(),i,value.getKey(),value.getValue().toString());
        jdbc.update("UPDATE remessas SET atividade=? WHERE id=?",now.toString(),remessa.id());
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa.id(),remessa.scenario(),remessa.ug(),number,"LOTE_RECEBIDO",now.toString());
        return new Lote(number,remessa.id(),remessa.key(),remessa.scenario(),remessa.ug(),remessa.yearMonth(),order,content.records(),content.subject(),hash,content.bytes(),now);
    }
    private static final String LOTE_SELECT="SELECT l.*,r.chave,r.competencia FROM lotes l JOIN remessas r ON r.id=l.remessa_id AND r.cenario=l.cenario AND r.ug=l.ug WHERE l.numero=? AND l.cenario=?";
    private Lote lote(java.sql.ResultSet r) throws java.sql.SQLException {
        return new Lote(r.getLong("numero"),r.getLong("remessa_id"),r.getString("chave"),r.getString("cenario"),r.getString("ug"),r.getInt("competencia"),
                r.getInt("ordem"),r.getInt("registros"),Assunto.valueOf(r.getString("assunto")),r.getString("hash"),r.getBytes("payload"),Instant.parse(r.getString("recebimento")));
    }
    public java.util.Optional<Lote> findLote(long number,String scenario,String ug) {
        return jdbc.query(LOTE_SELECT+" AND l.ug=?",(r,n)->lote(r),number,scenario,ug).stream().findFirst();
    }
    public java.util.Optional<Lote> inspectLote(long number,String scenario) {
        return jdbc.query(LOTE_SELECT,(r,n)->lote(r),number,scenario).stream().findFirst();
    }
    public java.util.List<RemessaEvent> events(long remessaId) {
        return jdbc.query("SELECT id,lote_id,tipo,instante FROM remessa_eventos WHERE remessa_id=? ORDER BY id",(r,n)->{
            long lote=r.getLong("lote_id"); Long loteId=r.wasNull()?null:lote; return new RemessaEvent(r.getLong("id"),loteId,r.getString("tipo"),Instant.parse(r.getString("instante")));
        },remessaId);
    }
    private void requireOpen(long id) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM remessas WHERE id=? AND estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL",Integer.class,id)!=1)
            throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
    }
    public Finalization.State finalization(Remessa remessa) {
        long count=jdbc.queryForObject("SELECT COUNT(*) FROM lotes WHERE ativo=1 AND remessa_id=? AND cenario=? AND ug=?",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
        long success=jdbc.queryForObject("SELECT COUNT(*) FROM lotes l JOIN processamento_trabalhos t ON t.lote_id=l.numero AND t.remessa_id=l.remessa_id AND t.cenario=l.cenario AND t.ug=l.ug JOIN scenarios s ON s.id=t.cenario AND s.generation=t.geracao WHERE l.remessa_id=? AND l.cenario=? AND l.ug=? AND l.ativo=1 AND t.situacao='PROCESSADO_SUCESSO'",Long.class,remessa.id(),remessa.scenario(),remessa.ug());
        var effective=new java.util.EnumMap<Assunto,Long>(Assunto.class);
        jdbc.query("SELECT l.assunto,COUNT(*) FROM registros_efetivos e JOIN lotes l ON l.numero=e.lote_id AND l.remessa_id=e.remessa_id AND l.cenario=e.cenario AND l.ug=e.ug WHERE l.ativo=1 AND e.remessa_id=? AND e.cenario=? AND e.ug=? GROUP BY l.assunto",r->{effective.put(Assunto.valueOf(r.getString(1)),r.getLong(2));},remessa.id(),remessa.scenario(),remessa.ug());
        return new Finalization.State(count,success,effective);
    }
    public void finish(Remessa remessa,Instant now) {
        if(jdbc.update("UPDATE remessas SET finalizada_em=?,atividade=? WHERE id=? AND cenario=? AND ug=? AND estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL",now.toString(),now.toString(),remessa.id(),remessa.scenario(),remessa.ug())!=1)
            throw new RemessaFailure(RemessaFailure.Kind.CONFLICT);
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,tipo,instante) VALUES (?,?,?,?,?)",remessa.id(),remessa.scenario(),remessa.ug(),"ENVIO_FINALIZADO",now.toString());
    }
    public void touch(Lote lote,Instant now) {
        if(jdbc.update("UPDATE remessas SET atividade=? WHERE id=? AND estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL",now.toString(),lote.remessaId())==0) return;
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",lote.remessaId(),lote.scenario(),lote.ug(),lote.number(),"LOTE_CONSULTADO",now.toString());
    }
}
