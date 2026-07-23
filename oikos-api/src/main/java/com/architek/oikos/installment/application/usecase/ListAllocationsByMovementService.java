package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.AllocationView;
import com.architek.oikos.installment.application.port.in.ListAllocationsByMovementUseCase;
import com.architek.oikos.installment.application.query.ListAllocationsByMovementQuery;
import com.architek.oikos.installment.domain.repository.AllocationRepository;

@Component
public class ListAllocationsByMovementService implements ListAllocationsByMovementUseCase {

    private final AllocationRepository allocationRepository;

    public ListAllocationsByMovementService(AllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationView> listAllocations(ListAllocationsByMovementQuery query) {
        return allocationRepository.findAllByMovementId(query.movementId()).stream()
                .map(AllocationView::from)
                .toList();
    }
}
