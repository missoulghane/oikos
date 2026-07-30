package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by property (via its own out-port,
 * UnitAccountProvisioningPort) to provision a unit's account as soon as the
 * Unit itself is created (AddUnitService/ConfigurePropertyService), so a
 * fund call never has to auto-create it lazily.
 */
public interface CreateUnitAccountUseCase {

    EntityId create(EntityId unitId, EntityId propertyId);
}
