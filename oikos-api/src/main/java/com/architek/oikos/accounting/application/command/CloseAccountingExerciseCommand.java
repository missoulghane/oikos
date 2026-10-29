package com.architek.oikos.accounting.application.command;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CloseAccountingExerciseCommand(EntityId propertyId, AccountingExerciseId exerciseId,
                                              EntityId closedByUserId) {
}
