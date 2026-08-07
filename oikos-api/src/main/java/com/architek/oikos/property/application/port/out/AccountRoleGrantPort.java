package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to grant a property-scoped role once a board seat is
 * validated. Implemented in property.infrastructure.adapter by delegating to
 * user's public port-in use cases - never to user's repository directly
 * (rule 6). targetRole is passed as a raw String to stay decoupled from
 * user.domain.model.PropertyRole - resolved back into that enum only in the
 * adapter, right before calling into user's port.
 */
public interface AccountRoleGrantPort {

    void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole);
}
