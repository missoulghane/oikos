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
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;

@Component
public class ResetPasswordService implements ResetPasswordUseCase {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserAccountPort userAccountPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final Clock clock;

    public ResetPasswordService(PasswordResetTokenRepository passwordResetTokenRepository,
                                 UserAccountPort userAccountPort,
                                 PasswordEncoderPort passwordEncoderPort,
                                 Clock clock) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userAccountPort = userAccountPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommand command) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid password reset token"));
        if (token.isExpired(clock.instant())) {
            throw new InvalidPasswordResetTokenException("Password reset token has expired");
        }
        HashedPassword newHashedPassword = passwordEncoderPort.encode(command.newPassword());
        userAccountPort.overwritePassword(token.userId(), newHashedPassword);
        passwordResetTokenRepository.deleteByUserId(token.userId());
    }
}
