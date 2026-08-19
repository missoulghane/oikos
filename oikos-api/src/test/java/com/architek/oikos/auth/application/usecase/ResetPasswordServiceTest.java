package com.architek.oikos.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.auth.application.command.ResetPasswordCommand;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.exception.InvalidPasswordResetTokenException;
import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    private static final PasswordResetTokenGenerator GENERATOR = new PasswordResetTokenGenerator();

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private ResetPasswordService newService() {
        return new ResetPasswordService(passwordResetTokenRepository, refreshTokenRepository, userAccountPort,
                passwordEncoderPort, GENERATOR, FIXED_CLOCK);
    }

    /** Ce que la base contient pour un lien donné : son empreinte, jamais sa valeur. */
    private static PasswordResetToken storedFor(String mailedToken, EntityId userId, Instant expiresAt) {
        return PasswordResetToken.issue(userId, GENERATOR.hash(mailedToken), expiresAt);
    }

    @Test
    void valid_token_overwrites_the_password_and_is_consumed() {
        EntityId userId = EntityId.newId();
        when(passwordResetTokenRepository.findByTokenHash(GENERATOR.hash("raw-token")))
                .thenReturn(Optional.of(storedFor("raw-token", userId, FIXED_CLOCK.instant().plusSeconds(3600))));
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));

        newService().resetPassword(new ResetPasswordCommand("raw-token", RawPassword.of("newpassword1")));

        verify(userAccountPort).overwritePassword(userId, HashedPassword.of("hashed"));
        verify(passwordResetTokenRepository).deleteByUserId(userId);
    }

    @Test
    void a_successful_reset_closes_every_open_session() {
        // Le sens même d'une réinitialisation : reprendre un compte dont on a perdu
        // le contrôle. Laisser vivre les jetons de rafraîchissement ne chasserait
        // personne.
        EntityId userId = EntityId.newId();
        when(passwordResetTokenRepository.findByTokenHash(GENERATOR.hash("raw-token")))
                .thenReturn(Optional.of(storedFor("raw-token", userId, FIXED_CLOCK.instant().plusSeconds(3600))));
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));

        newService().resetPassword(new ResetPasswordCommand("raw-token", RawPassword.of("newpassword1")));

        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    @Test
    void unknown_token_is_rejected() {
        when(passwordResetTokenRepository.findByTokenHash(GENERATOR.hash("bad-token"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().resetPassword(new ResetPasswordCommand("bad-token", RawPassword.of("newpassword1"))))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(userAccountPort, never()).overwritePassword(any(), any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void expired_token_is_rejected_and_password_is_not_overwritten() {
        EntityId userId = EntityId.newId();
        when(passwordResetTokenRepository.findByTokenHash(GENERATOR.hash("expired-token")))
                .thenReturn(Optional.of(storedFor("expired-token", userId, FIXED_CLOCK.instant().minusSeconds(1))));

        assertThatThrownBy(() -> newService().resetPassword(new ResetPasswordCommand("expired-token", RawPassword.of("newpassword1"))))
                .isInstanceOf(InvalidPasswordResetTokenException.class)
                .hasMessageContaining("expiré");
        verify(userAccountPort, never()).overwritePassword(any(), any());
        verify(passwordResetTokenRepository, never()).deleteByUserId(any());
        verify(refreshTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void the_raw_token_from_the_link_is_never_looked_up_as_is() {
        // Garde-fou contre un retour en arrière : chercher le jeton en clair
        // trouverait zéro ligne en base, la panne serait silencieuse.
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().resetPassword(new ResetPasswordCommand("raw-token", RawPassword.of("newpassword1"))))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(passwordResetTokenRepository, never()).findByTokenHash("raw-token");
    }

    @Test
    void password_reset_token_reports_expiry_correctly() {
        EntityId userId = EntityId.newId();
        PasswordResetToken token = storedFor("token", userId, FIXED_CLOCK.instant().plusSeconds(1));

        assertThat(token.isExpired(FIXED_CLOCK.instant())).isFalse();
        assertThat(token.isExpired(FIXED_CLOCK.instant().plusSeconds(2))).isTrue();
    }
}
