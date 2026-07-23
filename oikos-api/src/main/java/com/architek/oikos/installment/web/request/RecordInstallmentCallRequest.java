package com.architek.oikos.installment.web.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RecordInstallmentCallRequest(
        @NotNull LocalDate dueDate,
        @NotEmpty List<@Valid InstallmentCallLineRequest> lines) {
}
