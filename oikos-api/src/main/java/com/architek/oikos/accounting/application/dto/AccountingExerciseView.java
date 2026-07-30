package com.architek.oikos.accounting.application.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AccountingExerciseView(AccountingExerciseId id, EntityId propertyId, String label, LocalDate startDate,
                                      LocalDate endDate, ExerciseStatus status, Instant closedAt,
                                      EntityId closedByUserId, String comment) {

    public static AccountingExerciseView from(AccountingExercise exercise) {
        return new AccountingExerciseView(exercise.getId(), exercise.getPropertyId(), exercise.getLabel(),
                exercise.getStartDate(), exercise.getEndDate(), exercise.getStatus(), exercise.getClosedAt(),
                exercise.getClosedByUserId(), exercise.getComment());
    }
}
