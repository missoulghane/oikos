package com.architek.oikos.auth.infrastructure.adapter;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.valueobject.RefreshTokenId;
import com.architek.oikos.auth.infrastructure.persistence.RefreshTokenEntity;
import com.architek.oikos.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = jpaRepository.findById(refreshToken.id().asUuid())
                .orElseGet(RefreshTokenEntity::new);
        entity.setId(refreshToken.id().asUuid());
        entity.setUserId(refreshToken.userId().value());
        entity.setTokenHash(refreshToken.tokenHash());
        entity.setAuthorities(new HashSet<>(refreshToken.authorities()));
        entity.setExpiresAt(refreshToken.expiresAt());
        entity.setRevoked(refreshToken.revoked());
        RefreshTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return new RefreshToken(
                RefreshTokenId.of(entity.getId()),
                EntityId.of(entity.getUserId()),
                entity.getTokenHash(),
                entity.getAuthorities(),
                entity.getExpiresAt(),
                entity.isRevoked());
    }
}
