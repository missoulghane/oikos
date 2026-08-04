package com.architek.oikos.property.application.command;

import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.PropertyId;

public record UpdateDuesCalculationModeCommand(PropertyId id, DuesCalculationMode mode) {
}
