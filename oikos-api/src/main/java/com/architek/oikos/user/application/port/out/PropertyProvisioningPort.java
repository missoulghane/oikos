package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision a property (with its first building) and
 * assign its manager, on behalf of the property-manager registration flow.
 * Implemented in user.infrastructure.adapter by delegating to property's public
 * port-in use cases - never to property's repositories directly (rule 6).
 */
public interface PropertyProvisioningPort {

    EntityId provisionProperty(PropertyProvisioningDetails details);
}
