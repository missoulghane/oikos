package com.architek.oikos.installment.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.dto.AllocationView;
import com.architek.oikos.installment.application.port.in.ListAllocationsByInstallmentUseCase;
import com.architek.oikos.installment.application.query.ListAllocationsByInstallmentQuery;
import com.architek.oikos.installment.domain.repository.AllocationRepository;

@Component
public class ListAllocationsByInstallmentService implements ListAllocationsByInstallmentUseCase {

    private final AllocationRepository allocationRepository;

    public ListAllocationsByInstallmentService(AllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationView> listAllocations(ListAllocationsByInstallmentQuery query) {
        return allocationRepository.findAllByInstallmentId(query.installmentId()).stream()
                .map(AllocationView::from)
                .toList();
    }
}
