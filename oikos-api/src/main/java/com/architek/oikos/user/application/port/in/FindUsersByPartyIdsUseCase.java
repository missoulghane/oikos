package com.architek.oikos.user.application.port.in;

import java.util.Collection;
import java.util.Map;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by other features (e.g. messaging, to turn a
 * property member identified by partyId into a messaging recipient
 * identified by userId) to resolve which platform account a batch of Party
 * ids is linked to, without depending on the user repository directly (rule
 * 6). Batch counterpart of {@link FindLinkedPartyIdsUseCase}, returning the
 * actual userId rather than just membership.
 */
public interface FindUsersByPartyIdsUseCase {

    /** Only partyIds that do have a linked account appear as keys - never a null/absent value. */
    Map<EntityId, EntityId> findUserIdsByPartyIds(Collection<EntityId> partyIds);
}
