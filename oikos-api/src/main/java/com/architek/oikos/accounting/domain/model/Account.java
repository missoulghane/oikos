package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * An account's holder is either a Unit (a lot's own ledger - one account per
 * lot, not per co-owner) or a Property (the property's mirrored bookkeeping:
 * every movement on a unit account posts the opposite-direction movement
 * here - see AccountBalanceService). holderId is a generic cross-feature
 * reference: accounting does not depend on property's own UnitId/PropertyId
 * types (rule 4).
 * balance is persisted (kept in sync with every Movement by
 * AccountBalanceService) rather than always recomputed, for reporting/
 * performance reasons.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class Account {

    private final AccountId id;
    private final EntityId holderId;
    private final AccountType accountType;
    private final BigDecimal balance;

    private Account(AccountId id, EntityId holderId, AccountType accountType, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.holderId = Objects.requireNonNull(holderId, "holderId must not be null");
        this.accountType = Objects.requireNonNull(accountType, "accountType must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
    }

    public static Account create(AccountId id, EntityId holderId, AccountType accountType) {
        return new Account(id, holderId, accountType, BigDecimal.ZERO);
    }

    public static Account reconstruct(AccountId id, EntityId holderId, AccountType accountType, BigDecimal balance) {
        return new Account(id, holderId, accountType, balance);
    }

    /**
     * RG010: balance = Sum(CREDIT) - Sum(DEBIT) over the account's movements,
     * applied incrementally as each movement is recorded (AccountBalanceService)
     * rather than recomputed from the full ledger on every read.
     */
    public Account applyMovement(MovementDirection direction, BigDecimal amount) {
        BigDecimal delta = direction == MovementDirection.CREDIT ? amount : amount.negate();
        return new Account(id, holderId, accountType, balance.add(delta));
    }

    public AccountId getId() {
        return id;
    }

    public EntityId getHolderId() {
        return holderId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Account other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
