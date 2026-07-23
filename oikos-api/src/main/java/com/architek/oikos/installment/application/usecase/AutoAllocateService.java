package com.architek.oikos.installment.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.installment.application.command.AutoAllocateCommand;
import com.architek.oikos.installment.application.port.in.AutoAllocateUseCase;

@Component
public class AutoAllocateService implements AutoAllocateUseCase {

    private final AutoAllocationEngine autoAllocationEngine;

    public AutoAllocateService(AutoAllocationEngine autoAllocationEngine) {
        this.autoAllocationEngine = autoAllocationEngine;
    }

    @Override
    @Transactional
    public void allocate(AutoAllocateCommand command) {
        autoAllocationEngine.allocate(command.accountId());
    }
}
