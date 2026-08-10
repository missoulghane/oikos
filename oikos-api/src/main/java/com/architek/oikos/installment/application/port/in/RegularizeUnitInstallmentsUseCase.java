package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.RegularizeUnitInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizeUnitInstallmentsResult;

public interface RegularizeUnitInstallmentsUseCase {

    /** Imputes as much as possible of the unit's available advance onto its unsettled fund calls
     * (FIFO by due date), throwing NothingToRegularizeException if there is nothing to do. */
    RegularizeUnitInstallmentsResult regularize(RegularizeUnitInstallmentsCommand command);
}
