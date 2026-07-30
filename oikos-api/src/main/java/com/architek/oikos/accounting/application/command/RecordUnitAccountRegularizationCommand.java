package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record RecordUnitAccountRegularizationCommand(EntityId unitId, BigDecimal amount,
                                                       UnitAccountMovementDirection direction, String label,
                                                       String reason) {
}
