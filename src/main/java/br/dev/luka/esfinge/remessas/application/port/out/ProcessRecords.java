package br.dev.luka.esfinge.remessas.application.port.out;

import br.dev.luka.esfinge.remessas.domain.Lote;
import java.util.List;

public interface ProcessRecords { List<byte[]> process(Lote lote); }
