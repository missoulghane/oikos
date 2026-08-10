package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Provisions a unit's own dedicated receivable ledger account (role
 * UNIT_RECEIVABLE, number 341150 + increment) as soon as the unit is
 * created ("exigence supplementaire") - ADR 0001 decision 5: one account
 * per lot, not a shared collective account with an auxiliary.
 */
public interface ProvisionUnitReceivableAccountUseCase {

    LedgerAccountId provision(EntityId propertyId, EntityId unitId);
}
