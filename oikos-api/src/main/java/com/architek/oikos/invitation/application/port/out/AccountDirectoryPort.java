package com.architek.oikos.invitation.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to read an already-authenticated caller's identity and
 * grant the property-scoped role once a unit has been claimed. Implemented
 * in invitation.infrastructure.adapter by delegating to user's public
 * port-in use cases - never to user's repository directly (rule 6).
 */
public interface AccountDirectoryPort {

    AccountInfo getAccountInfo(EntityId userId);

    void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole);
}
