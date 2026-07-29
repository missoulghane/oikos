package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision a property (with its first building) and,
 * once its manager's Party exists, assign it as PROPERTY_MANAGER board
 * member, on behalf of the property-manager registration flow. Split in two
 * steps because the Party assigned as manager must be scoped to the
 * property being provisioned, so it can only be created once the property
 * id is known. Implemented in user.infrastructure.adapter by delegating to
 * property's public port-in use cases - never to property's repositories
 * directly (rule 6).
 */
public interface PropertyProvisioningPort {

    EntityId provisionProperty(PropertyProvisioningDetails details);

    void assignPropertyManager(EntityId propertyId, EntityId partyId);
}
