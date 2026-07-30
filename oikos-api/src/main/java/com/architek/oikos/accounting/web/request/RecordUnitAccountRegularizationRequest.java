package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;

public record RecordUnitAccountRegularizationRequest(@NotNull @Positive BigDecimal amount,
                                                       @NotNull UnitAccountMovementDirection direction,
                                                       @NotBlank String label, @NotBlank String reason) {
}
