package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A PCM chart-of-accounts entry (spec &sect;3.1). propertyId/unitId are null
 * for the shared "usage syndic" accounts seeded once (V11) and set only for
 * the property/unit-specific instances provisioned per ADR 0001 decision
 * 5/6 (a property's cash/bank accounts, a unit's dedicated receivable
 * account) - segregation of funds (spec &sect;1) is enforced by
 * JournalEntry.propertyId, not by duplicating the shared accounts per
 * property. normalSide is deliberately not a stored field: it is always
 * nature.normalSide() (spec &sect;3.1 ties nature and normal side one to
 * one), so there is nothing to keep in sync. balance is the account's net
 * amount expressed in its own normal side, kept up to date by
 * LedgerAccountRepository.incrementBalance() (an atomic SQL update) rather
 * than by loading, mutating and saving this aggregate - PostJournalEntryService
 * is the only writer, at the moment an entry becomes POSTED.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class LedgerAccount {

    private final LedgerAccountId id;
    private final EntityId propertyId;
    private final EntityId unitId;
    private final AccountNumber accountNumber;
    private final String label;
    private final int accountClass;
    private final AccountNature nature;
    private final boolean collective;
    private final AccountRole role;
    private final boolean active;
    private final BigDecimal balance;

    private LedgerAccount(LedgerAccountId id, EntityId propertyId, EntityId unitId, AccountNumber accountNumber,
                           String label, int accountClass, AccountNature nature, boolean collective,
                           AccountRole role, boolean active, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = propertyId;
        this.unitId = unitId;
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber must not be null");
        this.label = requireNonBlank(label, "label");
        if (accountClass < 1 || accountClass > 8) {
            throw new IllegalArgumentException("accountClass must be between 1 and 8: " + accountClass);
        }
        this.accountClass = accountClass;
        this.nature = Objects.requireNonNull(nature, "nature must not be null");
        this.collective = collective;
        this.role = role;
        this.active = active;
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        if (unitId != null && propertyId == null) {
            throw new IllegalArgumentException("a unit-scoped ledger account must also carry its property");
        }
    }

    public static LedgerAccount create(LedgerAccountId id, EntityId propertyId, EntityId unitId,
                                        AccountNumber accountNumber, String label, int accountClass,
                                        AccountNature nature, boolean collective, AccountRole role) {
        return new LedgerAccount(id, propertyId, unitId, accountNumber, label, accountClass, nature, collective,
                role, true, BigDecimal.ZERO);
    }

    public static LedgerAccount reconstruct(LedgerAccountId id, EntityId propertyId, EntityId unitId,
                                             AccountNumber accountNumber, String label, int accountClass,
                                             AccountNature nature, boolean collective, AccountRole role,
                                             boolean active, BigDecimal balance) {
        return new LedgerAccount(id, propertyId, unitId, accountNumber, label, accountClass, nature, collective,
                role, active, balance);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    /** I6: a directly-postable (non-collective) account never requires an auxiliary; a collective one always does. */
    public boolean requiresAuxiliary() {
        return collective;
    }

    public LedgerAccountId getId() {
        return id;
    }

    public Optional<EntityId> getPropertyId() {
        return Optional.ofNullable(propertyId);
    }

    public Optional<EntityId> getUnitId() {
        return Optional.ofNullable(unitId);
    }

    public AccountNumber getAccountNumber() {
        return accountNumber;
    }

    public String getLabel() {
        return label;
    }

    public int getAccountClass() {
        return accountClass;
    }

    public AccountNature getNature() {
        return nature;
    }

    public EntryDirection getNormalSide() {
        return nature.normalSide();
    }

    public boolean isCollective() {
        return collective;
    }

    public Optional<AccountRole> getRole() {
        return Optional.ofNullable(role);
    }

    public boolean isActive() {
        return active;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof LedgerAccount other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
