package com.architek.oikos.user.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.model.PartyInvitationToken;
import com.architek.oikos.user.domain.repository.PartyInvitationTokenRepository;
import com.architek.oikos.user.domain.valueobject.PartyInvitationTokenId;
import com.architek.oikos.user.infrastructure.persistence.PartyInvitationTokenEntity;
import com.architek.oikos.user.infrastructure.persistence.PartyInvitationTokenJpaRepository;

@Component
public class PartyInvitationTokenRepositoryAdapter implements PartyInvitationTokenRepository {

    private final PartyInvitationTokenJpaRepository jpaRepository;

    public PartyInvitationTokenRepositoryAdapter(PartyInvitationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PartyInvitationToken save(PartyInvitationToken token) {
        PartyInvitationTokenEntity entity = new PartyInvitationTokenEntity();
        entity.setId(token.id().asUuid());
        entity.setPartyId(token.partyId().value());
        entity.setEmail(token.email().value());
        entity.setFullName(token.fullName());
        entity.setToken(token.token());
        entity.setExpiresAt(token.expiresAt());
        PartyInvitationTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PartyInvitationToken> findByToken(String tokenValue) {
        return jpaRepository.findByToken(tokenValue).map(this::toDomain);
    }

    @Override
    public void deleteByPartyId(EntityId partyId) {
        jpaRepository.deleteByPartyId(partyId.value());
    }

    private PartyInvitationToken toDomain(PartyInvitationTokenEntity entity) {
        return new PartyInvitationToken(
                PartyInvitationTokenId.of(entity.getId()),
                EntityId.of(entity.getPartyId()),
                EmailVO.of(entity.getEmail()),
                entity.getFullName(),
                entity.getToken(),
                entity.getExpiresAt());
    }
}
