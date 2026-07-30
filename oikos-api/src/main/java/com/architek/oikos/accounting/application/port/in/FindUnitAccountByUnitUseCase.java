package com.architek.oikos.accounting.application.port.in;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by installment (via its own out-port,
 * UnitAccountLedgerPort) to resolve a unit's account id before posting a
 * fund-call debit. Cross-feature access must go through this port-in use
 * case, never through accounting's repository directly (rule 4/6).
 */
public interface FindUnitAccountByUnitUseCase {

    Optional<EntityId> findByUnitId(EntityId unitId);
}
