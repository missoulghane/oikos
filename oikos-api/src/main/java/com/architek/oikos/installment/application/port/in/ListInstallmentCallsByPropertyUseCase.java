package com.architek.oikos.installment.application.port.in;

import com.architek.oikos.installment.application.dto.InstallmentCallSummaryView;
import com.architek.oikos.installment.application.query.ListInstallmentCallsByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListInstallmentCallsByPropertyUseCase {

    Page<InstallmentCallSummaryView> listInstallmentCalls(ListInstallmentCallsByPropertyQuery query);
}
