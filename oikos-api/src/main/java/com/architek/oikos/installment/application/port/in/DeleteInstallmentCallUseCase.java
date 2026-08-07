package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.DeleteInstallmentCallCommand;

public interface DeleteInstallmentCallUseCase {

    void delete(DeleteInstallmentCallCommand command);
}
