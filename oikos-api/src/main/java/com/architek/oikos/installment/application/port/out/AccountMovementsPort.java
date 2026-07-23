package com.architek.oikos.installment.application.port.out;

import java.util.List;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used by AutoAllocationEngine to read an account's credit
 * movements (accounting's ledger) without depending on accounting's repositories
 * directly (rule 4). Implemented in installment.infrastructure.adapter by
 * delegating to accounting's public port-in (ListMovementsForAllocationUseCase).
 */
public interface AccountMovementsPort {

    List<AccountMovement> listCreditMovements(EntityId accountId);
}
