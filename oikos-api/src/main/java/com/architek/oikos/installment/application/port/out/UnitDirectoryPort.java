package com.architek.oikos.installment.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to verify that a unit referenced by a installment call
 * line actually exists. Implemented in installment.infrastructure.adapter by
 * delegating to property's public port-in (GetUnitUseCase), never to
 * property's repository directly (rule 4).
 */
public interface UnitDirectoryPort {

    boolean exists(EntityId unitId);

    /** Lot number as printed on documents ("A12"). */
    String getUnitNumber(EntityId unitId);

    /** Full names of the lot's owners; empty for a lot with none attached (OwnershipStatus.NOT_AFFECTED). */
    java.util.List<String> getOwnerFullNames(EntityId unitId);
}
