package br.dev.luka.esfinge.remessas.adapter.out.jdbc;

import br.dev.luka.esfinge.remessas.application.port.out.Transactions;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public final class JdbcTransactions implements Transactions {
    private final TransactionTemplate template;
    public JdbcTransactions(DataSource source) {
        var manager = new DataSourceTransactionManager(source);
        manager.setRollbackOnCommitFailure(true);
        template = new TransactionTemplate(manager);
    }
    public <T> T execute(Supplier<T> operation) { return template.execute(status -> operation.get()); }
}
