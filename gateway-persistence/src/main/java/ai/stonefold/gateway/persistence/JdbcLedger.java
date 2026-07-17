package ai.stonefold.gateway.persistence;

import ai.stonefold.gateway.core.TenantId;
import ai.stonefold.gateway.core.ledger.Ledger;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/**
 * Ledger over JDBC. tryDebit must be a single atomic statement
 * (conditional UPDATE / INSERT with a balance guard) so the budget bound
 * holds across concurrent replicas — no read-check-write in application code.
 */
public final class JdbcLedger implements Ledger {

    private final JdbcTemplate jdbc;

    public JdbcLedger(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean tryDebit(TenantId tenant, String account, BigDecimal amount, String reference) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public BigDecimal balance(TenantId tenant, String account) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
