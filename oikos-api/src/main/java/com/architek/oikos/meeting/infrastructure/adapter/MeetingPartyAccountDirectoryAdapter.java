package com.architek.oikos.meeting.infrastructure.adapter;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindUsersByPartyIdsUseCase;

/**
 * Cross-feature adapter delegating to user's public port-in, never its
 * repository (rule 6). Named with the Meeting prefix so the bean cannot
 * collide with messaging's adapter of the same shape.
 */
@Component
public class MeetingPartyAccountDirectoryAdapter implements PartyAccountDirectoryPort {

    private final FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase;

    public MeetingPartyAccountDirectoryAdapter(FindUsersByPartyIdsUseCase findUsersByPartyIdsUseCase) {
        this.findUsersByPartyIdsUseCase = findUsersByPartyIdsUseCase;
    }

    @Override
    public Map<EntityId, EntityId> resolveUserIds(Collection<EntityId> partyIds) {
        return findUsersByPartyIdsUseCase.findUserIdsByPartyIds(partyIds);
    }
}
