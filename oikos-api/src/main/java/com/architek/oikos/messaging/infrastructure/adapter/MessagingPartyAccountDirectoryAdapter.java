package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindUsersByPartyIdsUseCase;

/**
 * Cross-feature adapter: delegates to user's public port-in
 * (FindUsersByPartyIdsUseCase, the only change made outside the messaging
 * module - see plan section 1.3/docs/NOMENCLATURE.md), never to user's
 * repository directly (rule 6).
 */
@Component
public class MessagingPartyAccountDirectoryAdapter implements PartyAccountDirectoryPort {

    private final FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase;

    public MessagingPartyAccountDirectoryAdapter(FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase) {
        this.findUsersByPartyIdsUseCase = findUsersByPartyIdsUseCase;
    }

    @Override
    public Map<EntityId, EntityId> resolveUserIds(Collection<EntityId> partyIds) {
        return findUsersByPartyIdsUseCase.findUserIdsByPartyIds(partyIds);
    }
}
