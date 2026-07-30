package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferBetweenFinancialAccountsRequest(@NotBlank String fromAccountId, @NotBlank String toAccountId,
                                                        @NotNull @Positive BigDecimal amount, @NotNull LocalDate date,
                                                        @NotBlank String label) {
}
