package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.PropertyId;

public record SetProjectedBudgetCommand(PropertyId id, BigDecimal projectedBudget) {
}
