package com.architek.oikos.installment.application.command;

import java.time.LocalDate;
import java.time.YearMonth;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record GenerateInstallmentCallCommand(EntityId propertyId, YearMonth period, LocalDate dueDate) {
}
