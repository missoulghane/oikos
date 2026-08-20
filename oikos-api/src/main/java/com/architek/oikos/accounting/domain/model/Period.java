package com.architek.oikos.accounting.domain.model;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Objects;

import com.architek.oikos.accounting.domain.exception.PeriodAlreadyClosedException;
import com.architek.oikos.accounting.domain.exception.PeriodNotClosedException;
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
    private final Instant reopenedAt;
    private final EntityId reopenedByUserId;

    private Period(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth, PeriodStatus status,
                    Instant closedAt, EntityId closedByUserId, Instant reopenedAt, EntityId reopenedByUserId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.exerciseId = Objects.requireNonNull(exerciseId, "exerciseId must not be null");
        this.yearMonth = Objects.requireNonNull(yearMonth, "yearMonth must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.closedAt = closedAt;
        this.closedByUserId = closedByUserId;
        this.reopenedAt = reopenedAt;
        this.reopenedByUserId = reopenedByUserId;
    }

    public static Period open(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth) {
        return new Period(id, exerciseId, yearMonth, PeriodStatus.OPEN, null, null, null, null);
    }

    public static Period reconstruct(PeriodId id, AccountingExerciseId exerciseId, YearMonth yearMonth,
                                      PeriodStatus status, Instant closedAt, EntityId closedByUserId,
                                      Instant reopenedAt, EntityId reopenedByUserId) {
        return new Period(id, exerciseId, yearMonth, status, closedAt, closedByUserId, reopenedAt, reopenedByUserId);
    }

    public Period close(Instant closedAt, EntityId closedByUserId) {
        if (status == PeriodStatus.CLOSED) {
            throw new PeriodAlreadyClosedException(id);
        }
        return new Period(id, exerciseId, yearMonth, PeriodStatus.CLOSED, closedAt, closedByUserId, reopenedAt,
                reopenedByUserId);
    }

    /**
     * Rouvre une période close, pour corriger ce qui n'aurait pas dû l'être.
     *
     * <p>La date de clôture précédente est effacée - la période n'est plus
     * close, elle ne peut pas prétendre l'avoir été - mais la réouverture, elle,
     * laisse sa trace : qui, et quand. C'est le prix minimal d'un geste que la
     * comptabilité interdit d'ordinaire ; sans lui, le passé redeviendrait
     * modifiable sans que rien ne l'ait jamais montré.
     *
     * <p>L'enchaînement (ne rouvrir que la dernière période close, et seulement
     * dans un exercice ouvert) est une règle inter-périodes : elle vit dans le
     * use case, comme son symétrique à la clôture.
     */
    public Period reopen(Instant reopenedAt, EntityId reopenedByUserId) {
        if (status == PeriodStatus.OPEN) {
            throw new PeriodNotClosedException(id);
        }
        return new Period(id, exerciseId, yearMonth, PeriodStatus.OPEN, null, null, reopenedAt, reopenedByUserId);
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

    public Instant getReopenedAt() {
        return reopenedAt;
    }

    public EntityId getReopenedByUserId() {
        return reopenedByUserId;
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
