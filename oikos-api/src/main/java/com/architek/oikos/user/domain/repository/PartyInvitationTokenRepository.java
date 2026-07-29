package com.architek.oikos.user.domain.repository;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PartyInvitationToken;

public interface PartyInvitationTokenRepository {

    PartyInvitationToken save(PartyInvitationToken token);

    Optional<PartyInvitationToken> findByToken(String token);

    void deleteByPartyId(EntityId partyId);
}
