package com.architek.oikos.installment.application.command;

import java.time.LocalDate;
import java.util.List;

public record RecordInstallmentCallCommand(LocalDate dueDate, List<InstallmentCallLine> lines) {
}
