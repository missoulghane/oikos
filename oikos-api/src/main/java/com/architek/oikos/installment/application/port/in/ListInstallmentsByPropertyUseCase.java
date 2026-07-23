package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.query.ListInstallmentsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListInstallmentsByPropertyUseCase {

    Page<InstallmentView> listInstallments(ListInstallmentsByPropertyQuery query);
}
