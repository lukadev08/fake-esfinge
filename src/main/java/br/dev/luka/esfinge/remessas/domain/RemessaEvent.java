package br.dev.luka.esfinge.remessas.domain;

import java.time.Instant;
public record RemessaEvent(long id,Long lote,String type,Instant instant) {}
