package com.architek.oikos.accounting.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.application.port.out.AutoAllocationPort;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.installment.application.command.AutoAllocateCommand;
import com.architek.oikos.installment.application.port.in.AutoAllocateUseCase;

/**
 * Cross-feature adapter: triggers installment's FIFO auto-allocation by
 * delegating to its public port-in (AutoAllocateUseCase), never to
 * installment's repositories directly (rule 4).
 */
@Component
public class AccountingAutoAllocationAdapter implements AutoAllocationPort {

    private final AutoAllocateUseCase autoAllocateUseCase;

    public AccountingAutoAllocationAdapter(AutoAllocateUseCase autoAllocateUseCase) {
        this.autoAllocateUseCase = autoAllocateUseCase;
    }

    @Override
    public void allocate(AccountId accountId) {
        autoAllocateUseCase.allocate(new AutoAllocateCommand(accountId.value()));
    }
}
