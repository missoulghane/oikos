package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to reach the ledger (accounting) when raising a cotisation
 * call: resolving a unit's account, checking the property's mirror account
 * exists, and recording the triggering debit movement with its balance/mirror
 * side effects already applied (RG003/RG010/RG010bis) - all without
 * installment depending on accounting's repositories or domain model directly
 * (rule 4). Implemented in installment.infrastructure.adapter by delegating
 * to accounting's public port-in (FindAccountByHolderUseCase, RecordDebitUseCase).
 */
public interface AccountLedgerPort {

    Optional<EntityId> findUnitAccountId(EntityId unitId);

    boolean propertyAccountExists(EntityId propertyId);

    void recordDebit(EntityId accountId, BigDecimal amount, String label);
}
