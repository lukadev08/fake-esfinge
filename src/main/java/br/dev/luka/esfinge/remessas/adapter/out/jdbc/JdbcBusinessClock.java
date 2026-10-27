package br.dev.luka.esfinge.remessas.adapter.out.jdbc;

import br.dev.luka.esfinge.remessas.application.port.out.BusinessTime;
import br.dev.luka.esfinge.remessas.domain.RemessaFailure;
import java.time.*;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcBusinessClock extends Clock implements BusinessTime {
    private static final Instant FIRST_TIMESTAMP=Instant.parse("0000-01-01T00:00:00Z");
    private static final Instant LAST_TIMESTAMP=Instant.parse("9999-12-31T23:59:59.999999999Z");
    private final DataSource source;
    private final JdbcTemplate jdbc;
    private final String scenario;
    private final Clock base;
    public JdbcBusinessClock(DataSource source,String scenario,Clock base) {this.source=source;this.jdbc=new JdbcTemplate(source);this.scenario=scenario;this.base=base;}
    private long offset() {return jdbc.queryForObject("SELECT deslocamento_ms FROM relogio_negocio WHERE cenario=?",Long.class,scenario);}
    @Override public Instant instant() {var instant=base.instant().plusMillis(offset());validate(instant);return instant;}
    @Override public ZoneId getZone() {return base.getZone();}
    @Override public Clock withZone(ZoneId zone) {return new JdbcBusinessClock(source,scenario,base.withZone(zone));}
    public void validate(Instant instant) {
        if(instant.isBefore(FIRST_TIMESTAMP) || instant.isAfter(LAST_TIMESTAMP)
            || Duration.between(base.instant(),instant).compareTo(Duration.ofMillis(Long.MAX_VALUE))>0)
            throw new RemessaFailure(RemessaFailure.Kind.INVALID_REQUEST);
    }
    public void advance(long millis) {
        try {
            if(millis<0) throw new ArithmeticException();
            long next=Math.addExact(offset(),millis);validate(base.instant().plusMillis(next));
            jdbc.update("UPDATE relogio_negocio SET deslocamento_ms=? WHERE cenario=?",next,scenario);
        } catch(ArithmeticException|DateTimeException failure) {throw new RemessaFailure(RemessaFailure.Kind.INVALID_REQUEST);}
    }
}
