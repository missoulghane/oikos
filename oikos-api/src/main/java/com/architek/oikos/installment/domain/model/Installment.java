package com.architek.oikos.installment.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A due amount raised for a unit (RG003).
 * installmentCallId is nullable: set when raised in bulk by
 * GenerateInstallmentCallUseCase, left null for the manual, line-by-line
 * RecordInstallmentCallUseCase (no batch to attach to).
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class Installment {

    private final InstallmentId id;
    private final EntityId unitId;
    private final LocalDate dueDate;
    private final Amount amount;
    private final InstallmentCallId installmentCallId;

    private Installment(InstallmentId id, EntityId unitId, LocalDate dueDate, Amount amount,
                         InstallmentCallId installmentCallId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.unitId = Objects.requireNonNull(unitId, "unitId must not be null");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.installmentCallId = installmentCallId;
    }

    public static Installment create(InstallmentId id, EntityId unitId, LocalDate dueDate, Amount amount) {
        return new Installment(id, unitId, dueDate, amount, null);
    }

    public static Installment create(InstallmentId id, EntityId unitId, LocalDate dueDate, Amount amount,
                                      InstallmentCallId installmentCallId) {
        return new Installment(id, unitId, dueDate, amount, installmentCallId);
    }

    public static Installment reconstruct(InstallmentId id, EntityId unitId, LocalDate dueDate, Amount amount,
                                           InstallmentCallId installmentCallId) {
        return new Installment(id, unitId, dueDate, amount, installmentCallId);
    }

    public InstallmentId getId() {
        return id;
    }

    public EntityId getUnitId() {
        return unitId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Amount getAmount() {
        return amount;
    }

    public InstallmentCallId getInstallmentCallId() {
        return installmentCallId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Installment other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
