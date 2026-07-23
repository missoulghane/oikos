package com.architek.oikos.user.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to create/read/update the party (identity record) backing a
 * user account, and to resolve a party id from an email or phone number for
 * login-identifier resolution. Implemented in user.infrastructure.adapter by
 * delegating to party's public port-in use cases - never to party's repository
 * directly (rule 6). The generic {@link EntityId} keeps user.application fully
 * decoupled from party's own PartyId type.
 */
public interface PartyDirectoryPort {

    EntityId createParty(PartyDetails details);

    PartyDetails getPartyById(EntityId partyId);

    PartyDetails updateParty(EntityId partyId, PartyDetails details);

    Optional<EntityId> findIdByEmail(EmailVO email);

    Optional<EntityId> findIdByPhone(String phone);
}
