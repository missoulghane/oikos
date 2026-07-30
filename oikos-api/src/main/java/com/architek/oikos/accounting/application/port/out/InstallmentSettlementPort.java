package com.architek.oikos.accounting.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used when a lettrage validation settles some or all of a
 * fund call's outstanding amount: pushes the new outstanding amount back to
 * installment, without accounting depending on installment's repositories
 * or domain model directly (rule 4/6). Implemented in
 * accounting.infrastructure.adapter by delegating to installment's public
 * port-in (UpdateInstallmentSettlementUseCase) - the mirror, in the
 * opposite direction, of installment's own UnitAccountLedgerPort.
 */
public interface InstallmentSettlementPort {

    void updateOutstandingAmount(EntityId installmentId, BigDecimal outstandingAmount);
}
