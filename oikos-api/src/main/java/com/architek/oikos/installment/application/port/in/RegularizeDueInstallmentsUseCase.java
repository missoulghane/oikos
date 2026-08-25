package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.RegularizeDueInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeDueInstallmentsResult;

public interface RegularizeDueInstallmentsUseCase {

    RegularizeDueInstallmentsResult regularize(RegularizeDueInstallmentsCommand command);
}
