package com.architek.oikos.installment.application.port.in;

import java.util.List;

import com.architek.oikos.installment.application.dto.AllocationView;
import com.architek.oikos.installment.application.query.ListAllocationsByMovementQuery;

public interface ListAllocationsByMovementUseCase {

    List<AllocationView> listAllocations(ListAllocationsByMovementQuery query);
}
