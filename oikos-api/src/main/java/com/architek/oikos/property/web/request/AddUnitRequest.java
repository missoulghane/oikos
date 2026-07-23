package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddUnitRequest(
        @NotBlank @Size(max = 20) String unitNumber,
        @NotBlank String unitTypeId,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal shares) {
}
