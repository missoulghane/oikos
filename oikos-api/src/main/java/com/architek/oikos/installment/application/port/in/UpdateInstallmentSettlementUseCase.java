package com.architek.oikos.installment.application.port.in;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by accounting (via its own out-port,
 * InstallmentSettlementPort) to keep an installment's outstanding amount in
 * sync every time a lettrage validation settles some or all of its fund
 * call - the mirror, in the opposite direction, of installment's own
 * UnitAccountLedgerPort.recordDebit.
 */
public interface UpdateInstallmentSettlementUseCase {

    void update(EntityId installmentId, BigDecimal outstandingAmount);
}
