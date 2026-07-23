package com.architek.oikos.installment.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AllocatePaymentRequest(
        @NotBlank String movementId,
        @NotBlank String installmentId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
}
