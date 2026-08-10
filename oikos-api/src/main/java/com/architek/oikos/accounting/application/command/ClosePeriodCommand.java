package com.architek.oikos.accounting.application.command;

import java.time.YearMonth;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ClosePeriodCommand(EntityId propertyId, YearMonth period, EntityId closedByUserId) {
}
