package com.architek.oikos.accounting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountAllocationId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One "lettrage" line: a portion of a credit movement (payment or credit
 * regularization) applied against a debit movement (fund call), on the same
 * unit account. Immutable, never updated nor deleted once created - the
 * append-only reconciliation trail. Always produced by
 * LettrageProposalCalculator, never entered freely by a caller.
 */
public final class UnitAccountAllocation {

    private final UnitAccountAllocationId id;
    private final UnitAccountId unitAccountId;
    private final UnitAccountMovementId debitMovementId;
    private final UnitAccountMovementId creditMovementId;
    private final Amount amount;
    private final LocalDate allocatedDate;
    private final EntityId allocatedByUserId;

    private UnitAccountAllocation(UnitAccountAllocationId id, UnitAccountId unitAccountId,
                                   UnitAccountMovementId debitMovementId, UnitAccountMovementId creditMovementId,
                                   Amount amount, LocalDate allocatedDate, EntityId allocatedByUserId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.unitAccountId = Objects.requireNonNull(unitAccountId, "unitAccountId must not be null");
        this.debitMovementId = Objects.requireNonNull(debitMovementId, "debitMovementId must not be null");
        this.creditMovementId = Objects.requireNonNull(creditMovementId, "creditMovementId must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.allocatedDate = Objects.requireNonNull(allocatedDate, "allocatedDate must not be null");
        this.allocatedByUserId = Objects.requireNonNull(allocatedByUserId, "allocatedByUserId must not be null");
    }

    public static UnitAccountAllocation create(UnitAccountAllocationId id, UnitAccountId unitAccountId,
                                                UnitAccountMovementId debitMovementId,
                                                UnitAccountMovementId creditMovementId, Amount amount,
                                                LocalDate allocatedDate, EntityId allocatedByUserId) {
        return new UnitAccountAllocation(id, unitAccountId, debitMovementId, creditMovementId, amount, allocatedDate,
                allocatedByUserId);
    }

    public static UnitAccountAllocation reconstruct(UnitAccountAllocationId id, UnitAccountId unitAccountId,
                                                     UnitAccountMovementId debitMovementId,
                                                     UnitAccountMovementId creditMovementId, Amount amount,
                                                     LocalDate allocatedDate, EntityId allocatedByUserId) {
        return new UnitAccountAllocation(id, unitAccountId, debitMovementId, creditMovementId, amount, allocatedDate,
                allocatedByUserId);
    }

    public UnitAccountAllocationId getId() {
        return id;
    }

    public UnitAccountId getUnitAccountId() {
        return unitAccountId;
    }

    public UnitAccountMovementId getDebitMovementId() {
        return debitMovementId;
    }

    public UnitAccountMovementId getCreditMovementId() {
        return creditMovementId;
    }

    public Amount getAmount() {
        return amount;
    }

    public LocalDate getAllocatedDate() {
        return allocatedDate;
    }

    public EntityId getAllocatedByUserId() {
        return allocatedByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitAccountAllocation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
