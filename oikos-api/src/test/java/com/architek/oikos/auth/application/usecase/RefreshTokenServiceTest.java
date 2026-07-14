package com.architek.oikos.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.auth.application.command.RefreshTokenCommand;
import com.architek.oikos.auth.application.dto.AuthTokens;
import com.architek.oikos.auth.application.port.out.JwtTokenPort;
import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.RefreshTokenSecretGenerator;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.UnauthorizedException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenPort jwtTokenPort;

    private final RefreshTokenSecretGenerator secretGenerator = new RefreshTokenSecretGenerator();

    @Test
    void refresh_revokes_the_old_token_and_issues_a_new_pair() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, jwtTokenPort, secretGenerator,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 3600L);

        String rawToken = secretGenerator.generateOpaqueToken();
        String hash = secretGenerator.hash(rawToken);
        RefreshToken existing = RefreshToken.issue(EntityId.newId(), hash, Set.of("ROLE_USER"), Instant.EPOCH.plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenPort.generateAccessToken(any(), any())).thenReturn("new-access-token");
        when(jwtTokenPort.accessTokenTtlSeconds()).thenReturn(900L);

        AuthTokens tokens = service.refresh(new RefreshTokenCommand(rawToken));

        assertThat(tokens.accessToken()).isEqualTo("new-access-token");
        assertThat(tokens.refreshToken()).isNotEqualTo(rawToken);
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    void refresh_rejects_an_unknown_token() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, jwtTokenPort, secretGenerator,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 3600L);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshTokenCommand("unknown-token")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_rejects_an_expired_token() {
        RefreshTokenService service = new RefreshTokenService(refreshTokenRepository, jwtTokenPort, secretGenerator,
                Clock.fixed(Instant.EPOCH, ZoneOffset.UTC), 3600L);
        String rawToken = secretGenerator.generateOpaqueToken();
        String hash = secretGenerator.hash(rawToken);
        RefreshToken expired = RefreshToken.issue(EntityId.newId(), hash, Set.of(), Instant.EPOCH.minusSeconds(1));
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh(new RefreshTokenCommand(rawToken)))
                .isInstanceOf(UnauthorizedException.class);
    }
}
