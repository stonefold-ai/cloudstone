package ai.stonefold.gateway.core.ledger;

import ai.stonefold.gateway.core.TenantId;

import java.math.BigDecimal;

/**
 * Deterministic resource accounting per tenant (budgets, spend, quotas).
 * Reservations are atomic: a debit either fits within the remaining budget
 * and is recorded, or the whole action is refused — there is no
 * check-then-act gap even across replicas.
 */
public interface Ledger {

    /** @return true if the reservation fit and was recorded. */
    boolean tryDebit(TenantId tenant, String account, BigDecimal amount, String reference);

    BigDecimal balance(TenantId tenant, String account);
}
