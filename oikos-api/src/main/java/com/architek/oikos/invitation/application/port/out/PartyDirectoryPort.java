package com.architek.oikos.invitation.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve or create the party behind an invitation's
 * acceptance. Implemented in invitation.infrastructure.adapter by delegating
 * to party's public port-in use cases - never to party's repository
 * directly (rule 6).
 */
public interface PartyDirectoryPort {

    EntityId createParty(PartyDetails details, EntityId propertyId);

    Optional<EntityId> findIdByEmail(EmailVO email, EntityId propertyId);

    /** Optional.empty() si le contact n'existe pas (ou plus). */
    Optional<PartyContactInfo> findById(EntityId partyId);
}
