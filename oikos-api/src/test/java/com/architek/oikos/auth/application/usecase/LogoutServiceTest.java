package com.architek.oikos.auth.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.auth.application.command.LogoutCommand;
import com.architek.oikos.auth.domain.model.RefreshToken;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.RefreshTokenSecretGenerator;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private final RefreshTokenSecretGenerator secretGenerator = new RefreshTokenSecretGenerator();

    @Test
    void logout_revokes_the_matching_refresh_token() {
        LogoutService service = new LogoutService(refreshTokenRepository, secretGenerator);
        String rawToken = secretGenerator.generateOpaqueToken();
        String hash = secretGenerator.hash(rawToken);
        RefreshToken existing = RefreshToken.issue(EntityId.newId(), hash, Set.of(), Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(existing));

        service.logout(new LogoutCommand(rawToken));

        verify(refreshTokenRepository).save(argThatRevoked());
    }

    @Test
    void logout_is_a_no_op_when_token_is_unknown() {
        LogoutService service = new LogoutService(refreshTokenRepository, secretGenerator);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        service.logout(new LogoutCommand("unknown-token"));

        verify(refreshTokenRepository, never()).save(any());
    }

    private static RefreshToken argThatRevoked() {
        return org.mockito.ArgumentMatchers.argThat(RefreshToken::revoked);
    }
}
