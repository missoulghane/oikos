package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.command.RegularizePropertyInstallmentsCommand;
import com.architek.oikos.installment.application.dto.RegularizePropertyInstallmentsResult;

public interface RegularizePropertyInstallmentsUseCase {

    /** Sweeps every unit of the property carrying an available advance and regularizes it against
     * its unsettled fund calls; units with nothing to do are silently skipped (not an error). */
    RegularizePropertyInstallmentsResult regularize(RegularizePropertyInstallmentsCommand command);
}
