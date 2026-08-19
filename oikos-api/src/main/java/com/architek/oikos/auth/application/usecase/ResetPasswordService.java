package com.architek.oikos.auth.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.auth.application.command.ResetPasswordCommand;
import com.architek.oikos.auth.application.port.in.ResetPasswordUseCase;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.exception.InvalidPasswordResetTokenException;
import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.repository.RefreshTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;

/**
 * Consumes a reset link: checks the token, sets the new password, then closes
 * every session the account had open.
 *
 * <p>That last step is not housekeeping. A reset is what someone reaches for when
 * they have lost control of their account, and a refresh token already handed out
 * would otherwise outlive the password it was obtained with - the intruder would
 * keep the account the owner just took back.
 *
 * <p>Its two messages are in French, unlike the rest of the codebase: they are
 * returned as-is to the reader (see GlobalExceptionHandler, which puts
 * getMessage() in the response body), at the very moment that reader is already
 * stuck outside their account.
 */
@Component
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountPort userAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final Clock clock;

    public ResetPasswordService(PasswordResetTokenRepository passwordResetTokenRepository,
                                 RefreshTokenRepository refreshTokenRepository,
                                 UserAccountPort userAccountPort,
                                 PasswordEncoderPort passwordEncoderPort,
                                 PasswordResetTokenGenerator tokenGenerator,
                                 Clock clock) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountPort = userAccountPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        // The stored row holds the hash, so the token from the link is hashed
        // before the lookup - never the other way round.
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(tokenGenerator.hash(command.token()))
                .orElseThrow(() -> new InvalidPasswordResetTokenException(
                        "Ce lien de réinitialisation n'est pas valide. Demandez-en un nouveau."));
        if (token.isExpired(clock.instant())) {
            throw new InvalidPasswordResetTokenException(
                    "Ce lien de réinitialisation a expiré. Demandez-en un nouveau.");
        }
        HashedPassword newHashedPassword = passwordEncoderPort.encode(command.newPassword());
        userAccountPort.overwritePassword(token.userId(), newHashedPassword);
        passwordResetTokenRepository.deleteByUserId(token.userId());
        refreshTokenRepository.deleteByUserId(token.userId());
    }
}
