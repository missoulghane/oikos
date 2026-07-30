package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to reach the ledger (accounting) when raising an
 * installment: resolving the unit's account, then recording the triggering
 * debit movement with its balance side effects already applied (spec
 * &sect;10), all without installment depending on accounting's repositories
 * or domain model directly (rule 4/6). Implemented in
 * installment.infrastructure.adapter by delegating to accounting's public
 * port-in (FindUnitAccountByUnitUseCase, RecordUnitAccountDebitUseCase).
 */
public interface UnitAccountLedgerPort {

    Optional<EntityId> findUnitAccountId(EntityId unitId);

    void recordDebit(EntityId unitAccountId, BigDecimal amount, String label, EntityId installmentId);
}
