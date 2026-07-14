package com.architek.oikos.auth.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.auth.application.command.RefreshTokenCommand;
import com.architek.oikos.auth.application.dto.AuthTokens;
import com.architek.oikos.auth.application.port.in.RefreshTokenUseCase;
import com.architek.oikos.auth.application.port.out.JwtTokenPort;
import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.RefreshTokenSecretGenerator;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Rotation: the presented refresh token is revoked and a brand new pair is issued,
 * even though the access token is short-lived and does not itself need rotation.
 */
@Component
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenSecretGenerator secretGenerator;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                JwtTokenPort jwtTokenPort,
                                RefreshTokenSecretGenerator secretGenerator,
                                Clock clock,
                                @Value("${oikos.security.jwt.refresh-token-ttl-seconds}") long refreshTokenTtlSeconds) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenPort = jwtTokenPort;
        this.secretGenerator = secretGenerator;
        this.clock = clock;
        this.refreshTokenTtl = Duration.ofSeconds(refreshTokenTtlSeconds);
    }

    @Override
    @Transactional
    public AuthTokens refresh(RefreshTokenCommand command) {
        String incomingHash = secretGenerator.hash(command.refreshToken());
        RefreshToken existing = refreshTokenRepository.findByTokenHash(incomingHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (!existing.isUsable(clock.instant())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        refreshTokenRepository.save(existing.revoke());

        String newAccessToken = jwtTokenPort.generateAccessToken(existing.userId(), existing.authorities());
        String rawNewRefreshToken = secretGenerator.generateOpaqueToken();
        String newHash = secretGenerator.hash(rawNewRefreshToken);
        Instant expiresAt = clock.instant().plus(refreshTokenTtl);
        refreshTokenRepository.save(RefreshToken.issue(existing.userId(), newHash, existing.authorities(), expiresAt));

        return new AuthTokens(newAccessToken, rawNewRefreshToken, jwtTokenPort.accessTokenTtlSeconds());
    }
}
