package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.architek.oikos.property.domain.valueobject.UnitType;

public record AddUnitRequest(
        @NotBlank @Size(max = 20) String unitNumber,
        @NotNull UnitType unitType,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal shares) {
}
