package com.architek.oikos.accounting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * A single entry in a unit account's ledger (spec &sect;8). Immutable: a
 * movement is never updated nor physically deleted once created (spec
 * &sect;17) - it is the append-only source of truth the unit account balance
 * is always computed from. reason is required for REGULARIZATION (spec
 * &sect;13: "necessite un motif obligatoire"), forbidden otherwise (it would
 * be a meaningless field for a FUND_CALL/PAYMENT).
 */
public final class UnitAccountMovement {

    private final UnitAccountMovementId id;
    private final AccountingExerciseId exerciseId;
    private final UnitAccountId unitAccountId;
    private final LocalDate date;
    private final UnitAccountMovementType type;
    private final UnitAccountMovementDirection direction;
    private final Amount amount;
    private final String businessReference;
    private final String label;
    private final String reason;

    private UnitAccountMovement(UnitAccountMovementId id, AccountingExerciseId exerciseId,
                                 UnitAccountId unitAccountId, LocalDate date, UnitAccountMovementType type,
                                 UnitAccountMovementDirection direction, Amount amount, String businessReference,
                                 String label, String reason) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.unitAccountId = Objects.requireNonNull(unitAccountId, "unitAccountId must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.businessReference = businessReference;
        this.label = requireNonBlank(label, "label");
        if (type == UnitAccountMovementType.REGULARIZATION && (reason == null || reason.isBlank())) {
            throw new IllegalArgumentException("reason is required for a REGULARIZATION movement");
        }
        if (type != UnitAccountMovementType.REGULARIZATION && reason != null) {
            throw new IllegalArgumentException("reason is only meaningful for a REGULARIZATION movement");
        }
        this.reason = reason;
    }

    public static UnitAccountMovement create(UnitAccountMovementId id, AccountingExerciseId exerciseId,
                                              UnitAccountId unitAccountId, LocalDate date,
                                              UnitAccountMovementType type, UnitAccountMovementDirection direction,
                                              Amount amount, String businessReference, String label, String reason) {
        return new UnitAccountMovement(id, exerciseId, unitAccountId, date, type, direction, amount,
                businessReference, label, reason);
    }

    public static UnitAccountMovement reconstruct(UnitAccountMovementId id, AccountingExerciseId exerciseId,
                                                   UnitAccountId unitAccountId, LocalDate date,
                                                   UnitAccountMovementType type,
                                                   UnitAccountMovementDirection direction, Amount amount,
                                                   String businessReference, String label, String reason) {
        return new UnitAccountMovement(id, exerciseId, unitAccountId, date, type, direction, amount,
                businessReference, label, reason);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public UnitAccountMovementId getId() {
        return id;
    }

    public AccountingExerciseId getExerciseId() {
        return exerciseId;
    }

    public UnitAccountId getUnitAccountId() {
        return unitAccountId;
    }

    public LocalDate getDate() {
        return date;
    }

    public UnitAccountMovementType getType() {
        return type;
    }

    public UnitAccountMovementDirection getDirection() {
        return direction;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getBusinessReference() {
        return businessReference;
    }

    public String getLabel() {
        return label;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof UnitAccountMovement other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
