package com.architek.oikos.installment.application.command;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;

public record DeleteInstallmentCallCommand(InstallmentCallId id) {
}
