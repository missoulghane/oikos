package com.architek.oikos.messaging.application.port.out;

import java.util.Collection;
import java.util.Map;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to resolve which platform account (userId) a party is
 * linked to, turning a property member (identified by partyId, as returned
 * by PropertyMemberDirectoryPort) into an actual messaging recipient
 * (identified by userId). Implemented in
 * messaging.infrastructure.adapter.MessagingPartyAccountDirectoryAdapter,
 * delegating to the new user.application.port.in.FindUsersByPartyIdsUseCase
 * (see docs/NOMENCLATURE.md / plan section 1.3 - the only change outside the
 * messaging module).
 */
public interface PartyAccountDirectoryPort {

    /** Only partyIds that do have a linked account appear as keys - never a null/absent value. */
    Map<EntityId, EntityId> resolveUserIds(Collection<EntityId> partyIds);
}
