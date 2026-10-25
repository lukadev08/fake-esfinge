package br.dev.luka.esfinge.remessas.application.port.in;

import br.dev.luka.esfinge.remessas.domain.*;

public interface Remessas { Remessa open(String token, int yearMonth, String cancellation); Lote receive(String token,String key,ReceivedContent content);
    Lote consult(String token,long number);
    LoteInspection inspect(String adminKey,long number); }
