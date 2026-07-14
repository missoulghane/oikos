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
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    private ResetPasswordService newService() {
        return new ResetPasswordService(passwordResetTokenRepository, userAccountPort, passwordEncoderPort, FIXED_CLOCK);
    }

    @Test
    void valid_token_overwrites_the_password_and_is_consumed() {
        EntityId userId = EntityId.newId();
        PasswordResetToken token = PasswordResetToken.issue(userId, "raw-token", FIXED_CLOCK.instant().plusSeconds(3600));
        when(passwordResetTokenRepository.findByToken("raw-token")).thenReturn(Optional.of(token));
        when(passwordEncoderPort.encode(any())).thenReturn(HashedPassword.of("hashed"));

        newService().resetPassword(new ResetPasswordCommand("raw-token", RawPassword.of("newpassword1")));

        verify(userAccountPort).overwritePassword(userId, HashedPassword.of("hashed"));
        verify(passwordResetTokenRepository).deleteByUserId(userId);
    }

    @Test
    void unknown_token_is_rejected() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().resetPassword(new ResetPasswordCommand("bad-token", RawPassword.of("newpassword1"))))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(userAccountPort, never()).overwritePassword(any(), any());
    }

    @Test
    void expired_token_is_rejected_and_password_is_not_overwritten() {
        EntityId userId = EntityId.newId();
        PasswordResetToken token = PasswordResetToken.issue(userId, "expired-token", FIXED_CLOCK.instant().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> newService().resetPassword(new ResetPasswordCommand("expired-token", RawPassword.of("newpassword1"))))
                .isInstanceOf(InvalidPasswordResetTokenException.class);
        verify(userAccountPort, never()).overwritePassword(any(), any());
        verify(passwordResetTokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void password_reset_token_reports_expiry_correctly() {
        EntityId userId = EntityId.newId();
        PasswordResetToken token = PasswordResetToken.issue(userId, "token", FIXED_CLOCK.instant().plusSeconds(1));

        assertThat(token.isExpired(FIXED_CLOCK.instant())).isFalse();
        assertThat(token.isExpired(FIXED_CLOCK.instant().plusSeconds(2))).isTrue();
    }
}
