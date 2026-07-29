package com.architek.oikos.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyInvitationTokenJpaRepository extends JpaRepository<PartyInvitationTokenEntity, UUID> {

    Optional<PartyInvitationTokenEntity> findByToken(String token);

    void deleteByPartyId(UUID partyId);
}
