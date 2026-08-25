package com.architek.oikos.user.domain.repository;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PartyInvitationToken;

public interface PartyInvitationTokenRepository {

    PartyInvitationToken save(PartyInvitationToken token);

    Optional<PartyInvitationToken> findByToken(String token);

    /**
     * L'invitation en cours pour ce contact, s'il y en a une - au plus une a la
     * fois, chaque envoi supprimant la precedente (voir InvitePartyService).
     * Peut etre expiree : c'est a l'appelant de le regarder.
     */
    Optional<PartyInvitationToken> findByPartyId(EntityId partyId);

    void deleteByPartyId(EntityId partyId);
}
