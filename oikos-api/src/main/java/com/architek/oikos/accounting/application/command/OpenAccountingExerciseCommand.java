package com.architek.oikos.accounting.application.command;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OpenAccountingExerciseCommand(EntityId propertyId, String label, LocalDate startDate,
                                             LocalDate endDate, String comment) {
}
