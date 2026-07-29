package com.architek.oikos.user.application.port.in;

import java.util.Collection;
import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by other features (e.g. property, for the
 * "Contacts" tab) to know which of a batch of Party ids already have a
 * linked AppUser account, without depending on the user repository directly
 * (rule 6). Batch counterpart of {@link InvitePartyUseCase}'s no-op check.
 */
public interface FindLinkedPartyIdsUseCase {

    Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds);
}
