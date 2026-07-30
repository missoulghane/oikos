package com.architek.oikos.accounting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A unit's (lot's) current account (spec &sect;7): one per lot, not per
 * co-owner (installment.unit_id is already lot-scoped, no impedance
 * mismatch). balance is persisted (kept in sync with every
 * UnitAccountMovement) rather than always recomputed, for reporting/
 * performance reasons - never modified manually. Negative balance = the
 * owner owes money; positive = credit/advance; zero = settled.
 * unitId/propertyId are generic cross-feature references: accounting does
 * not depend on property's own UnitId/PropertyId types (rule 4/6).
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class UnitAccount {

    private final UnitAccountId id;
    private final EntityId unitId;
    private final EntityId propertyId;
    private final BigDecimal balance;
    private final Instant lastUpdatedDate;

    private UnitAccount(UnitAccountId id, EntityId unitId, EntityId propertyId, BigDecimal balance,
                         Instant lastUpdatedDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
        this.lastUpdatedDate = Objects.requireNonNull(lastUpdatedDate, "lastUpdatedDate must not be null");
    }

    public static UnitAccount create(UnitAccountId id, EntityId unitId, EntityId propertyId, Instant now) {
        return new UnitAccount(id, unitId, propertyId, BigDecimal.ZERO, now);
    }

    public static UnitAccount reconstruct(UnitAccountId id, EntityId unitId, EntityId propertyId, BigDecimal balance,
                                           Instant lastUpdatedDate) {
        return new UnitAccount(id, unitId, propertyId, balance, lastUpdatedDate);
    }

    /** RG (spec &sect;7/&sect;10/&sect;11): balance = Sum(CREDIT) - Sum(DEBIT), applied incrementally. */
    public UnitAccount applyMovement(UnitAccountMovementDirection direction, BigDecimal amount, Instant now) {
        BigDecimal delta = direction == UnitAccountMovementDirection.CREDIT ? amount : amount.negate();
        return new UnitAccount(id, unitId, propertyId, balance.add(delta), now);
    }

    public UnitAccountId getId() {
        return id;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitAccount other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
