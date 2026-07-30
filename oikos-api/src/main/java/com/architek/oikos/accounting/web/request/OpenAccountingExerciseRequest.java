package com.architek.oikos.accounting.web.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpenAccountingExerciseRequest(@NotBlank @Size(max = 200) String label, @NotNull LocalDate startDate,
                                             @NotNull LocalDate endDate, @Size(max = 1000) String comment) {
}
