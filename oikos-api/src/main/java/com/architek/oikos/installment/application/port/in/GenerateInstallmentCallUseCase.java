package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.GenerateInstallmentCallCommand;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;

public interface GenerateInstallmentCallUseCase {

    GenerateInstallmentCallResult generate(GenerateInstallmentCallCommand command);
}
