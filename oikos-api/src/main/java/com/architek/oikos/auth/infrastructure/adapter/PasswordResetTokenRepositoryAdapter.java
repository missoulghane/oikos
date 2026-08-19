package com.architek.oikos.auth.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.valueobject.PasswordResetTokenId;
import com.architek.oikos.auth.infrastructure.persistence.PasswordResetTokenEntity;
import com.architek.oikos.auth.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setId(token.id().asUuid());
        entity.setUserId(token.userId().value());
        entity.setTokenHash(token.tokenHash());
        entity.setExpiresAt(token.expiresAt());
        PasswordResetTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(EntityId userId) {
        jpaRepository.deleteByUserId(userId.value());
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
                PasswordResetTokenId.of(entity.getId()),
                EntityId.of(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt());
    }
}
