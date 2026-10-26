package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision a property and, once the Party of the
 * account that created it exists, seat that Party on the property's board.
 * Split in two steps because the Party must be scoped to the property being
 * provisioned, so it can only be created once the property id is known.
 * Implemented in user.infrastructure.adapter by delegating to property's
 * public port-in use cases - never to property's repositories directly
 * (rule 6).
 */
public interface PropertyProvisioningPort {

    EntityId provisionProperty(PropertyProvisioningDetails details);

    /** Professional firm flows: the Party sits on the board as PROPERTY_MANAGER. */
    void assignPropertyManager(EntityId propertyId, EntityId partyId);

    /** Volunteer-syndic wizard: the Party sits on the board in the seat it chose. */
    void assignBoardMember(EntityId propertyId, EntityId partyId, BoardSeat seat);
}
