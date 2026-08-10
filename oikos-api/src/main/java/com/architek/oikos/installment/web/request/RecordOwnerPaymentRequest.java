package com.architek.oikos.installment.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.installment.domain.valueobject.PaymentMode;

public record RecordOwnerPaymentRequest(@NotNull PaymentMode mode, @NotBlank String treasuryAccountId,
                                         @NotNull LocalDate valueDate,
                                         @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount) {
}
