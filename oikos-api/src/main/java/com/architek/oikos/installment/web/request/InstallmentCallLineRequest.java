package com.architek.oikos.installment.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InstallmentCallLineRequest(
        @NotBlank String unitId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
}
