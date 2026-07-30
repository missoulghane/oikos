package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountStatus;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A real treasury account of the property (cash box, bank account, mobile
 * money) - what the property actually holds. balance is persisted (kept in
 * sync with every FinancialJournalEntry) rather than always recomputed, for
 * reporting/performance reasons (spec &sect;4) - never modified manually.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class FinancialAccount {

    private final FinancialAccountId id;
    private final EntityId propertyId;
    private final String name;
    private final FinancialAccountType type;
    private final String currency;
    private final BigDecimal balance;
    private final FinancialAccountStatus status;

    private FinancialAccount(FinancialAccountId id, EntityId propertyId, String name, FinancialAccountType type,
                              String currency, BigDecimal balance, FinancialAccountStatus status) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.name = requireNonBlank(name, "name");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.currency = requireNonBlank(currency, "currency");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public static FinancialAccount create(FinancialAccountId id, EntityId propertyId, String name,
                                           FinancialAccountType type, String currency) {
        return new FinancialAccount(id, propertyId, name, type, currency, BigDecimal.ZERO,
                FinancialAccountStatus.ACTIVE);
    }

    public static FinancialAccount reconstruct(FinancialAccountId id, EntityId propertyId, String name,
                                                FinancialAccountType type, String currency, BigDecimal balance,
                                                FinancialAccountStatus status) {
        return new FinancialAccount(id, propertyId, name, type, currency, balance, status);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    /** Solde = Solde initial + mouvements entrants - mouvements sortants (spec &sect;4). */
    public FinancialAccount applyEntry(FinancialEntryDirection direction, BigDecimal amount) {
        BigDecimal delta = direction == FinancialEntryDirection.IN ? amount : amount.negate();
        return new FinancialAccount(id, propertyId, name, type, currency, balance.add(delta), status);
    }

    public FinancialAccountId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public String getName() {
        return name;
    }

    public FinancialAccountType getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public FinancialAccountStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof FinancialAccount other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
