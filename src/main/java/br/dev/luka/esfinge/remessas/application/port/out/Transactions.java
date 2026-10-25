package br.dev.luka.esfinge.remessas.application.port.out;

import java.util.function.Supplier;

public interface Transactions { <T> T execute(Supplier<T> operation); }
