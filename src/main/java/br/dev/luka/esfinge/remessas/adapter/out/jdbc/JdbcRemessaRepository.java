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
        return new Remessa(id, key, scenario, ug, yearMonth, now, now);
    }
    public java.util.Optional<Remessa> find(String key,String scenario,String ug) {
        return jdbc.query("SELECT id,chave,cenario,ug,competencia,abertura,atividade FROM remessas WHERE chave=? AND cenario=? AND ug=? AND estado='ABERTO'",
                (r,n) -> new Remessa(r.getLong("id"),r.getString("chave"),r.getString("cenario"),r.getString("ug"),r.getInt("competencia"),
                        Instant.parse(r.getString("abertura")),Instant.parse(r.getString("atividade"))),key,scenario,ug).stream().findFirst();
    }
    public Lote receive(Remessa remessa,ReceivedContent content,String hash,Instant now) {
        int order=jdbc.queryForObject("SELECT COALESCE(MAX(ordem),0)+1 FROM lotes WHERE remessa_id=?",Integer.class,remessa.id());
        long number=jdbc.queryForObject("INSERT INTO lotes(remessa_id,cenario,ug,ordem,assunto,payload,hash,registros,recebimento) VALUES (?,?,?,?,?,?,?,?,?) RETURNING numero",Long.class,
                remessa.id(),remessa.scenario(),remessa.ug(),order,"CADASTRO_CONTRIBUINTE",content.bytes(),hash,content.records(),now.toString());
        jdbc.update("UPDATE remessas SET atividade=? WHERE id=?",now.toString(),remessa.id());
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",remessa.id(),remessa.scenario(),remessa.ug(),number,"LOTE_RECEBIDO",now.toString());
        return new Lote(number,remessa.id(),remessa.key(),remessa.scenario(),remessa.ug(),remessa.yearMonth(),order,content.records(),hash,content.bytes(),now);
    }
    private static final String LOTE_SELECT="SELECT l.*,r.chave,r.competencia FROM lotes l JOIN remessas r ON r.id=l.remessa_id AND r.cenario=l.cenario AND r.ug=l.ug WHERE l.numero=? AND l.cenario=?";
    private Lote lote(java.sql.ResultSet r) throws java.sql.SQLException {
        return new Lote(r.getLong("numero"),r.getLong("remessa_id"),r.getString("chave"),r.getString("cenario"),r.getString("ug"),r.getInt("competencia"),
                r.getInt("ordem"),r.getInt("registros"),r.getString("hash"),r.getBytes("payload"),Instant.parse(r.getString("recebimento")));
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
    public void touch(Lote lote,Instant now) {
        jdbc.update("UPDATE remessas SET atividade=? WHERE id=?",now.toString(),lote.remessaId());
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",lote.remessaId(),lote.scenario(),lote.ug(),lote.number(),"LOTE_CONSULTADO",now.toString());
    }
}
