package com.architek.oikos.accounting.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to verify that a unit referenced by an accounting
 * operation actually exists. Implemented in accounting.infrastructure.adapter
 * by delegating to property's public port-in (GetUnitUseCase), never to
 * property's repository directly (rule 4/6).
 */
public interface UnitDirectoryPort {

    boolean exists(EntityId unitId);
}
