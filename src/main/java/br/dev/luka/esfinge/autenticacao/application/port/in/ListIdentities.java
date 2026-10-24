package br.dev.luka.esfinge.autenticacao.application.port.in;

import br.dev.luka.esfinge.autenticacao.domain.Account;
import java.util.List;

public interface ListIdentities { List<Account> identities(); }
