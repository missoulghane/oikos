package com.architek.oikos.property.application.port.out;

import java.util.Collection;
import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to invite a unit owner's Party to link an AppUser
 * account, right after that Party is resolved/created, and to check which
 * parties already have one. Implemented in property.infrastructure.adapter
 * by delegating to user's public port-in use cases - never to user's
 * repository directly (rule 6).
 */
public interface AccountLinkingPort {

    void inviteOwnerIfUnlinked(EntityId partyId, EmailVO email, String fullName);

    /**
     * @return the subset of {@code partyIds} that already have a linked AppUser account.
     */
    Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds);
}
