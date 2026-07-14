package com.architek.oikos.user.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.valueobject.UserId;
import com.architek.oikos.user.domain.valueobject.VerificationTokenId;
import com.architek.oikos.user.infrastructure.persistence.VerificationTokenEntity;
import com.architek.oikos.user.infrastructure.persistence.VerificationTokenJpaRepository;

@Component
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepository {

    private final VerificationTokenJpaRepository jpaRepository;

    public VerificationTokenRepositoryAdapter(VerificationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenEntity entity = new VerificationTokenEntity();
        entity.setId(token.id().asUuid());
        entity.setUserId(token.userId().asUuid());
        entity.setToken(token.token());
        entity.setExpiresAt(token.expiresAt());
        VerificationTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<VerificationToken> findByToken(String tokenValue) {
        return jpaRepository.findByToken(tokenValue).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(UserId userId) {
        jpaRepository.deleteByUserId(userId.asUuid());
    }

    private VerificationToken toDomain(VerificationTokenEntity entity) {
        return new VerificationToken(
                VerificationTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getToken(),
                entity.getExpiresAt());
    }
}
