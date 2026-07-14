package com.architek.oikos.auth.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.auth.application.command.RequestPasswordResetCommand;
import com.architek.oikos.auth.application.port.in.RequestPasswordResetUseCase;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;

/**
 * Intentionally silent when no account matches the email: the web layer always
 * answers 202 regardless, so as not to leak account existence (same rationale as
 * user.application.usecase.ResendVerificationService).
 */
@Component
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private final UserAccountPort userAccountPort;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final PasswordResetEmailComposer emailComposer;
    private final Clock clock;
    private final Duration passwordResetTokenTtl;

    public RequestPasswordResetService(UserAccountPort userAccountPort,
                                        PasswordResetTokenRepository passwordResetTokenRepository,
                                        EmailSenderPort emailSenderPort,
                                        PasswordResetTokenGenerator tokenGenerator,
                                        PasswordResetEmailComposer emailComposer,
                                        Clock clock,
                                        @Value("${oikos.mail.password-reset-token-ttl-hours}") long passwordResetTokenTtlHours) {
        this.userAccountPort = userAccountPort;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.passwordResetTokenTtl = Duration.ofHours(passwordResetTokenTtlHours);
    }

    @Override
    @Transactional
    public void requestReset(RequestPasswordResetCommand command) {
        userAccountPort.findIdByEmail(command.email()).ifPresent(userId -> {
            passwordResetTokenRepository.deleteByUserId(userId);
            String rawToken = tokenGenerator.generate();
            Instant expiresAt = clock.instant().plus(passwordResetTokenTtl);
            PasswordResetToken resetToken = PasswordResetToken.issue(userId, rawToken, expiresAt);
            passwordResetTokenRepository.save(resetToken);
            emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        });
    }
}
