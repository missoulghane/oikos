package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotNull;
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;

public record UpdateDuesCalculationModeRequest(@NotNull DuesCalculationMode mode) {
}
