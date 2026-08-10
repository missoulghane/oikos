package com.architek.oikos.accounting.domain.model;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

import com.architek.oikos.accounting.domain.exception.PeriodAlreadyClosedException;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A monthly sub-period of an AccountingExercise (spec &sect;4.1: "periodes
 * mensuelles avec statut propre"). The cross-period invariant "a period only
 * closes if the previous one is already closed" needs sibling periods and is
 * therefore an application-layer concern (P8), not enforced here.
 * Immutable: every mutation returns a new instance. Entity semantics:
 * equals/hashCode are identity-based (on id), not value-based.
 */
public final class Period {

    private final PeriodId id;
    private final AccountingExerciseId exerciseId;
    private final YearMonth yearMonth;
    private final PeriodStatus status;
    private final Instant closedAt;
    private final EntityId closedByUserId;

    private Period(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth, PeriodStatus status,
                    Instant closedAt, EntityId closedByUserId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.yearMonth = Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.closedAt = closedAt;
        this.closedByUserId = closedByUserId;
    }

    public static Period open(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth) {
        return new Period(id, exerciseId, yearMonth, PeriodStatus.OPEN, null, null);
    }

    public static Period reconstruct(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth,
                                      PeriodStatus status, Instant closedAt, EntityId closedByUserId) {
        return new Period(id, exerciseId, yearMonth, status, closedAt, closedByUserId);
    }

    public Period close(Instant closedAt, EntityId closedByUserId) {
        if (status == PeriodStatus.CLOSED) {
            throw new PeriodAlreadyClosedException(id);
        }
        return new Period(id, exerciseId, yearMonth, PeriodStatus.CLOSED, closedAt, closedByUserId);
    }

    public boolean isOpen() {
        return status == PeriodStatus.OPEN;
    }

    public PeriodId getId() {
        return id;
    }

    public AccountingExerciseId getExerciseId() {
        return exerciseId;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public PeriodStatus getStatus() {
        return status;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public EntityId getClosedByUserId() {
        return closedByUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Period other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
