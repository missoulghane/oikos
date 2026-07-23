package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.InstallmentCallDetailView;
import com.architek.oikos.installment.application.query.GetInstallmentCallQuery;

public interface GetInstallmentCallUseCase {

    InstallmentCallDetailView getInstallmentCall(GetInstallmentCallQuery query);
}
