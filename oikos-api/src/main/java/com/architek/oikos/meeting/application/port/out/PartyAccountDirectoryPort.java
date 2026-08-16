package com.architek.oikos.meeting.application.port.out;

import java.util.Collection;
import java.util.Map;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Resolves which platform accounts a batch of parties are linked to, so an
 * in-app notification can be addressed to the owners who actually have one.
 * Same patron as messaging's port of the same name; only partyIds that do have
 * an account appear as keys.
 */
public interface PartyAccountDirectoryPort {

    Map<EntityId, EntityId> resolveUserIds(Collection<EntityId> partyIds);
}
