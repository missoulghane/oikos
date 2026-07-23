package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.query.GetInstallmentQuery;

public interface GetInstallmentUseCase {

    InstallmentView getInstallment(GetInstallmentQuery query);
}
