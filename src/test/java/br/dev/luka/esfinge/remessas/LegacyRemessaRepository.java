package br.dev.luka.esfinge.remessas;

import br.dev.luka.esfinge.remessas.application.port.out.RemessaRepository;
import br.dev.luka.esfinge.remessas.domain.*;
import java.time.Instant;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

// SQL anterior somente nas operações de fixture que precisam ler/escrever remessas sem a coluna V9.
final class LegacyRemessaRepository implements RemessaRepository {
    private final JdbcTemplate jdbc;
    LegacyRemessaRepository(DataSource source) { jdbc = new JdbcTemplate(source);current=new br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository(source); }
    private final br.dev.luka.esfinge.remessas.adapter.out.jdbc.JdbcRemessaRepository current;
    public boolean blocked(String scenario,String ug,int yearMonth) {return false;}
    public void block(String scenario,String ug,int yearMonth,boolean blocked) {throw new UnsupportedOperationException();}
    public Remessa open(String scenario,String ug,int yearMonth,String key,Instant now) {
        long id=jdbc.queryForObject("INSERT INTO remessas(chave,cenario,ug,competencia,abertura,atividade) VALUES(?,?,?,?,?,?) RETURNING id",Long.class,key,scenario,ug,yearMonth,now.toString(),now.toString());
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,tipo,instante) VALUES(?,?,?,?,?)",id,scenario,ug,"ENVIO_ABERTO",now.toString());
        return new Remessa(id,key,scenario,ug,yearMonth,now,now,null,null,Remessa.deadline(now),null);
    }
    public java.util.Optional<Remessa> findOpen(String scenario,String ug,int yearMonth) {return java.util.Optional.empty();}
    public java.util.List<Remessa> due(String scenario,Instant now) {return java.util.List.of();}
    public java.util.Optional<Remessa> find(String key,String scenario) {throw new UnsupportedOperationException();}
    public java.util.Optional<Reception.Plan> receptionPlan(Remessa remessa,Assunto subject) {return java.util.Optional.empty();}
    public void planReception(Remessa remessa,Assunto subject,Reception.Type type,long attempt,Instant now) {throw new UnsupportedOperationException();}
    public void removeReceptionPlan(Remessa remessa,Assunto subject,Instant now) {throw new UnsupportedOperationException();}
    public Reception.Type receptionAttempt(Remessa remessa,Assunto subject) {return null;}
    public void responseLost(Remessa remessa,Lote lot,Instant now) {throw new UnsupportedOperationException();}
    public java.util.Optional<Remessa> find(String key,String scenario,String ug) {
        return jdbc.query("SELECT id,chave,cenario,ug,competencia,abertura,atividade FROM remessas WHERE chave=? AND cenario=? AND ug=? AND estado='ABERTO'",
                (r,n) -> new Remessa(r.getLong("id"),r.getString("chave"),r.getString("cenario"),r.getString("ug"),r.getInt("competencia"),
                        Instant.parse(r.getString("abertura")),Instant.parse(r.getString("atividade")),null,null,Remessa.deadline(Instant.parse(r.getString("atividade"))),null),key,scenario,ug).stream().findFirst();
    }
    public Lote receive(Remessa remessa,ReceivedContent content,String hash,Instant now) {
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
    public java.util.Optional<Lote> findLote(long number,String scenario,String ug) {return current.findLote(number,scenario,ug);}
    public java.util.Optional<Lote> inspectLote(long number,String scenario) {return current.inspectLote(number,scenario);}
    public java.util.List<RemessaEvent> events(long remessaId) {return current.events(remessaId);}
    public Finalization.State finalization(Remessa remessa) {throw new UnsupportedOperationException();}
    public void finish(Remessa remessa,Instant now) {throw new UnsupportedOperationException();}
    public void touch(Lote lote,Instant now) {
        jdbc.update("UPDATE remessas SET atividade=? WHERE id=?",now.toString(),lote.remessaId());
        jdbc.update("INSERT INTO remessa_eventos(remessa_id,cenario,ug,lote_id,tipo,instante) VALUES (?,?,?,?,?,?)",lote.remessaId(),lote.scenario(),lote.ug(),lote.number(),"LOTE_CONSULTADO",now.toString());
    }
}
