package com.architek.oikos.auth.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.auth.application.command.RequestPasswordResetCommand;
import com.architek.oikos.auth.application.port.in.RequestPasswordResetUseCase;
import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.auth.domain.repository.PasswordResetTokenRepository;
import com.architek.oikos.auth.domain.service.PasswordResetTokenGenerator;
import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.EmailSendQuotaPort;

/**
 * Intentionally silent to the *caller* when no account matches the email: the web
 * layer always answers 202 regardless, so as not to leak account existence (same
 * rationale as user.application.usecase.ResendVerificationService). That miss is
 * logged though - the anti-enumeration guarantee covers the HTTP response, not
 * the server's own logs.
 */
@Component
public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private static final Logger log = LoggerFactory.getLogger(RequestPasswordResetService.class);

    private final UserAccountPort userAccountPort;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final EmailSendQuotaPort emailSendQuotaPort;
    private final PasswordResetTokenGenerator tokenGenerator;
    private final PasswordResetEmailComposer emailComposer;
    private final Clock clock;
    private final Duration passwordResetTokenTtl;

    public RequestPasswordResetService(UserAccountPort userAccountPort,
                                        PasswordResetTokenRepository passwordResetTokenRepository,
                                        EmailSenderPort emailSenderPort,
                                        EmailSendQuotaPort emailSendQuotaPort,
                                        PasswordResetTokenGenerator tokenGenerator,
                                        PasswordResetEmailComposer emailComposer,
                                        Clock clock,
                                        @Value("${oikos.mail.password-reset-token-ttl-hours}") long passwordResetTokenTtlHours) {
        this.userAccountPort = userAccountPort;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.emailSendQuotaPort = emailSendQuotaPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.passwordResetTokenTtl = Duration.ofHours(passwordResetTokenTtlHours);
    }

    @Override
    @Transactional
    public void requestReset(RequestPasswordResetCommand command) {
        // Before the lookup, never after: a quota applied only to the addresses
        // that exist would answer, by its very refusal, the question this endpoint
        // spends the rest of its code refusing to answer.
        emailSendQuotaPort.requireQuota(command.email().value());
        userAccountPort.findIdByEmail(command.email()).ifPresentOrElse(userId -> {
            passwordResetTokenRepository.deleteByUserId(userId);
            String rawToken = tokenGenerator.generate();
            Instant expiresAt = clock.instant().plus(passwordResetTokenTtl);
            // Only the hash is stored; rawToken exists from here to the email and
            // nowhere else - it is the one copy the recipient will ever hold.
            PasswordResetToken resetToken = PasswordResetToken.issue(userId, tokenGenerator.hash(rawToken), expiresAt);
            passwordResetTokenRepository.save(resetToken);
            emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        },
        // The endpoint answers 202 either way, on purpose: telling the caller
        // "unknown address" would turn this into an account-enumeration oracle.
        // That protection covers the HTTP response, not the server's own logs -
        // so say it plainly here, otherwise a reset requested for a typo'd
        // address is indistinguishable from a mailer that is silently broken.
        () -> log.info("Password reset requested for an unknown address ({}) - no email sent", command.email().value()));
    }
}
