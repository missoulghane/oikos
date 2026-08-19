package com.architek.oikos.auth.domain.repository;

import java.util.Optional;

import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Closes every session an account has open. Called on password reset: a reset is
     * also how someone takes back an account they lost control of, and leaving the
     * refresh tokens already handed out alive would chase nobody out.
     */
    void deleteByUserId(EntityId userId);
}
