package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateUnitSharesRequest(
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal shares) {
}
