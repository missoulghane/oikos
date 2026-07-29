package com.architek.oikos.property.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve or create the party behind a unit owner, and to
 * read a party's identity for display. Implemented in property.infrastructure.adapter
 * by delegating to party's public port-in use cases - never to party's repository
 * directly (rule 6). The generic {@link EntityId} keeps property.application fully
 * decoupled from party's own PartyId type.
 */
public interface PartyDirectoryPort {

    EntityId createParty(PartyDetails details, EntityId propertyId);

    Optional<EntityId> findIdByEmail(EmailVO email, EntityId propertyId);

    PartyDetails getPartyById(EntityId partyId);
}
