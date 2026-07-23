package com.architek.oikos.accounting.application.port.out;

import com.architek.oikos.accounting.domain.valueobject.AccountId;

/**
 * Outbound port used to trigger FIFO auto-allocation (installment module's
 * AutoAllocationEngine) after recording a credit movement - RecordPaymentService
 * needs a payment to potentially settle existing due installments without
 * accounting depending on installment's repositories directly (rule 4).
 * Implemented in accounting.infrastructure.adapter by delegating to installment's
 * public port-in (AutoAllocateUseCase), never to installment's repository
 * directly.
 */
public interface AutoAllocationPort {

    void allocate(AccountId accountId);
}
