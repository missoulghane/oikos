package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.dto.AllocationView;
import com.architek.oikos.installment.application.query.ListAllocationsByInstallmentQuery;

public interface ListAllocationsByInstallmentUseCase {

    List<AllocationView> listAllocations(ListAllocationsByInstallmentQuery query);
}
