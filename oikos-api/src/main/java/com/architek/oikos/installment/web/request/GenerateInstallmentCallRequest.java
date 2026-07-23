package com.architek.oikos.installment.web.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record GenerateInstallmentCallRequest(
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}", message = "must be in yyyy-MM format") String period,
        @NotNull LocalDate dueDate) {
}
