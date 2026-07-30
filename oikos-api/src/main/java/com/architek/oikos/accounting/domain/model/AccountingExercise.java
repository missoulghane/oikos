package com.architek.oikos.accounting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A property's accounting period: every new accounting operation is attached
 * to the property's single open exercise (RG §3). Closing an exercise (RG
 * §15 - computing closing balances, generating "Report a nouveau" opening
 * entries) is a separate, larger use case not covered by this Phase 1 - only
 * opening and enforcing "open" as a precondition for writes is in scope.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class AccountingExercise {

    private final AccountingExerciseId id;
    private final EntityId propertyId;
    private final String label;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final ExerciseStatus status;
    private final Instant closedAt;
    private final EntityId closedByUserId;
    private final String comment;

    private AccountingExercise(AccountingExerciseId id, EntityId propertyId, String label, LocalDate startDate,
                                LocalDate endDate, ExerciseStatus status, Instant closedAt, EntityId closedByUserId,
                                String comment) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.propertyId = Objects.requireNonNull(propertyId, "propertyId must not be null");
        this.label = requireNonBlank(label, "label");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.endDate = Objects.requireNonNull(endDate, "endDate must not be null");
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.closedAt = closedAt;
        this.closedByUserId = closedByUserId;
        this.comment = comment;
    }

    public static AccountingExercise open(AccountingExerciseId id, EntityId propertyId, String label,
                                           LocalDate startDate, LocalDate endDate, String comment) {
        return new AccountingExercise(id, propertyId, label, startDate, endDate, ExerciseStatus.OPEN, null, null,
                comment);
    }

    public static AccountingExercise reconstruct(AccountingExerciseId id, EntityId propertyId, String label,
                                                  LocalDate startDate, LocalDate endDate, ExerciseStatus status,
                                                  Instant closedAt, EntityId closedByUserId, String comment) {
        return new AccountingExercise(id, propertyId, label, startDate, endDate, status, closedAt, closedByUserId,
                comment);
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public boolean isOpen() {
        return status == ExerciseStatus.OPEN;
    }

    public AccountingExerciseId getId() {
        return id;
    }

    public EntityId getPropertyId() {
        return propertyId;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public ExerciseStatus getStatus() {
        return status;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public EntityId getClosedByUserId() {
        return closedByUserId;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof AccountingExercise other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
