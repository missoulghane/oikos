package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record SetUnitTypePriceRequest(
        @NotNull @DecimalMin(value = "0.00") BigDecimal price) {
}
