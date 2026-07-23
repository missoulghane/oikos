package com.architek.oikos.accounting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to verify that a unit referenced by a installment call
 * line (or a UNIT-type account's holder) actually exists, and to resolve
 * which property a unit belongs to (needed to mirror a unit account's
 * movements onto its property account - see AccountBalanceService).
 * Implemented in accounting.infrastructure.adapter by delegating to property's
 * public port-in (GetUnitUseCase, GetBuildingUseCase), never to property's
 * repository directly (rule 4).
 */
public interface UnitDirectoryPort {

    boolean exists(EntityId unitId);

    EntityId resolvePropertyId(EntityId unitId);
}
