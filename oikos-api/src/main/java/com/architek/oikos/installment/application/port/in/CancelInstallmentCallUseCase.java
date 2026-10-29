package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.CancelInstallmentCallCommand;

public interface CancelInstallmentCallUseCase {

    void cancel(CancelInstallmentCallCommand command);
}
