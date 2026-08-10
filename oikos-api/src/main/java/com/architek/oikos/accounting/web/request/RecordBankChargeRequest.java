package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordBankChargeRequest(@NotNull LocalDate pieceDate, @NotBlank String ledgerAccountId,
                                       @NotBlank String bankAccountId,
                                       @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal amount,
                                       @Size(max = 1000) String description) {
}
