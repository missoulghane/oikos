package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision the accounting account of a Unit as soon
 * as it is created, so a fund call never has to auto-create it lazily.
 * Implemented in property.infrastructure.adapter by delegating to
 * accounting's public port-in use case, never to accounting's repository
 * directly (rule 4/6). The generic EntityId keeps property.application fully
 * decoupled from accounting's own UnitAccountId type.
 */
public interface UnitAccountProvisioningPort {

    void provisionAccount(EntityId unitId, EntityId propertyId);
}
