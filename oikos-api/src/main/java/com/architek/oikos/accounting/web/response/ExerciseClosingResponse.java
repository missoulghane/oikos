package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;

import com.architek.oikos.accounting.application.dto.ExerciseClosingView;

/** {@code closingEntryId} est nul quand l'exercice n'avait ni produit ni charge à solder. */
public record ExerciseClosingResponse(AccountingExerciseResponse exercise, BigDecimal netResult,
                                       String closingEntryId) {

    public static ExerciseClosingResponse from(ExerciseClosingView view) {
        return new ExerciseClosingResponse(AccountingExerciseResponse.from(view.exercise()), view.netResult(),
                view.closingEntryId() == null ? null : view.closingEntryId().toString());
    }
}
