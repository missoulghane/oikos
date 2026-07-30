package com.architek.oikos.accounting.web.response;

import java.time.Instant;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;

public record AccountingExerciseResponse(String id, String propertyId, String label, LocalDate startDate,
                                          LocalDate endDate, ExerciseStatus status, Instant closedAt,
                                          String closedByUserId, String comment) {

    public static AccountingExerciseResponse from(AccountingExerciseView view) {
        return new AccountingExerciseResponse(view.id().toString(), view.propertyId().toString(), view.label(),
                view.startDate(), view.endDate(), view.status(), view.closedAt(),
                view.closedByUserId() == null ? null : view.closedByUserId().toString(), view.comment());
    }
}
