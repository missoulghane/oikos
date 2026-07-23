package com.architek.oikos.installment.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.DeallocateCommand;
import com.architek.oikos.installment.application.port.in.DeallocateUseCase;
import com.architek.oikos.installment.domain.exception.AllocationNotFoundException;
import com.architek.oikos.installment.domain.repository.AllocationRepository;

/**
 * RG012: removes an allocation without touching any movement - the ledger
 * stays untouched, only the matching record disappears, freeing both the
 * movement's available credit and the installment's remaining due for a
 * future (re)allocation.
 */
@Component
public class DeallocateService implements DeallocateUseCase {

    private final AllocationRepository allocationRepository;

    public DeallocateService(AllocationRepository allocationRepository) {
        this.allocationRepository = allocationRepository;
    }

    @Override
    @Transactional
    public void deallocate(DeallocateCommand command) {
        allocationRepository.findById(command.id())
                .orElseThrow(() -> new AllocationNotFoundException(command.id()));
        allocationRepository.deleteById(command.id());
    }
}
