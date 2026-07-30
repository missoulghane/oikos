package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecordExceptionalDepositRequest(@NotBlank String financialAccountId, @NotNull @Positive BigDecimal amount,
                                               @NotNull LocalDate date, @NotBlank @Size(max = 200) String label) {
}
