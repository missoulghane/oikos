package com.architek.oikos.accounting.application.port.in;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by installment (via its own out-port,
 * UnitAccountLedgerPort) to post the FUND_CALL debit movement triggered by
 * raising an Installment (spec &sect;10), in the same transaction as the
 * Installment's own creation. installmentId is stored as the movement's
 * businessReference (traceability), never as a foreign key accounting
 * depends on directly (rule 4/6).
 */
public interface RecordUnitAccountDebitUseCase {

    void recordDebit(EntityId unitAccountId, BigDecimal amount, String label, EntityId installmentId);
}
