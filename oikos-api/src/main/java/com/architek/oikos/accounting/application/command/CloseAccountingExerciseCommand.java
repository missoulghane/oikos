package com.architek.oikos.accounting.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CloseAccountingExerciseCommand(EntityId propertyId, EntityId closedByUserId) {
}
