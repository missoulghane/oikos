package com.architek.oikos.auth.domain.repository;

import java.util.Optional;

import com.architek.oikos.auth.domain.model.RefreshToken;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
