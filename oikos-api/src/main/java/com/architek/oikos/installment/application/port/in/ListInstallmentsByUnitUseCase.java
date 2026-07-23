package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.query.ListInstallmentsByUnitQuery;

public interface ListInstallmentsByUnitUseCase {

    List<InstallmentView> listInstallments(ListInstallmentsByUnitQuery query);
}
