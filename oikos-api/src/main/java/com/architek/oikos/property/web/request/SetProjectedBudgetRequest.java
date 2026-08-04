package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetProjectedBudgetRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal projectedBudget) {
}
