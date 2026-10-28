package br.dev.luka.esfinge.autenticacao.adapter.out.jdbc;

import br.dev.luka.esfinge.autenticacao.application.port.out.AuthenticationTransactions;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public final class JdbcAuthenticationTransactions implements AuthenticationTransactions {
    private final TransactionTemplate template;
    public JdbcAuthenticationTransactions(DataSource source) {
        var manager=new DataSourceTransactionManager(source);
        manager.setRollbackOnCommitFailure(true);
        template=new TransactionTemplate(manager);
    }
    public <T> T execute(Supplier<T> operation) {return template.execute(status->operation.get());}
}
