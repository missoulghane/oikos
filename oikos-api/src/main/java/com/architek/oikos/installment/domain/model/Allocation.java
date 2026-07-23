package com.architek.oikos.installment.domain.model;

import java.util.Objects;

import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Links part of a credit Movement to an Installment it settles (RG009: an
 * allocation never alters either side - it is purely a matching record).
 * Unlike Movement, an allocation may be physically deleted: deallocating then
 * reallocating a payment (RG012) must not touch the immutable ledger, and the
 * account balance (computed from movements only, RG010) is unaffected by
 * allocation changes.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class Allocation {

    private final AllocationId id;
    private final EntityId movementId;
    private final InstallmentId installmentId;
    private final Amount allocatedAmount;

    private Allocation(AllocationId id, EntityId movementId, InstallmentId installmentId, Amount allocatedAmount) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.movementId = Objects.requireNonNull(movementId, "movementId must not be null");
        this.installmentId = Objects.requireNonNull(installmentId, "installmentId must not be null");
        this.allocatedAmount = Objects.requireNonNull(allocatedAmount, "allocatedAmount must not be null");
    }

    public static Allocation create(AllocationId id, EntityId movementId, InstallmentId installmentId, Amount allocatedAmount) {
        return new Allocation(id, movementId, installmentId, allocatedAmount);
    }

    public static Allocation reconstruct(AllocationId id, EntityId movementId, InstallmentId installmentId, Amount allocatedAmount) {
        return new Allocation(id, movementId, installmentId, allocatedAmount);
    }

    public AllocationId getId() {
        return id;
    }

    public EntityId getMovementId() {
        return movementId;
    }

    public InstallmentId getInstallmentId() {
        return installmentId;
    }

    public Amount getAllocatedAmount() {
        return allocatedAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Allocation other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
