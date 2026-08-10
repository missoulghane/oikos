package com.architek.oikos.installment.application.port.in;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point kept for the accounting PCM engine (ADR 0001) to keep
 * an installment's outstanding amount in sync once a payment allocation
 * settles some or all of its fund call. Not called by anything yet - the
 * old lettrage integration that used to invoke it (accounting's
 * InstallmentSettlementPort) was removed with the rest of the pre-PCM
 * model; Phase 4 (installment's own Payment/Allocation use cases) is
 * expected to call it directly, in-module, once designed.
 */
public interface UpdateInstallmentSettlementUseCase {

    void update(EntityId installmentId, BigDecimal outstandingAmount);
}
