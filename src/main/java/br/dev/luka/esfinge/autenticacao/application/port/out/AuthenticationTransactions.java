package br.dev.luka.esfinge.autenticacao.application.port.out;

import java.util.function.Supplier;

public interface AuthenticationTransactions { <T> T execute(Supplier<T> operation); }
