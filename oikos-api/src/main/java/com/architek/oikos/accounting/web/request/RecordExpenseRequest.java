package com.architek.oikos.accounting.web.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecordExpenseRequest(@NotBlank String financialAccountId, @NotNull LocalDate date,
                                    @NotBlank @Size(max = 100) String category, @NotBlank @Size(max = 200) String provider,
                                    @NotNull @Positive BigDecimal amount, @Size(max = 1000) String description,
                                    @Size(max = 200) String receiptReference) {
}
