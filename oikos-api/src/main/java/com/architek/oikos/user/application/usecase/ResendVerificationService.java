package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.EmailSendQuotaPort;
import com.architek.oikos.user.application.command.ResendVerificationCommand;
import com.architek.oikos.user.application.port.in.ResendVerificationUseCase;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

/**
 * Intentionally silent when the account does not exist or is already verified: the
 * web layer always answers 202 regardless, so as not to leak account existence.
 */
@Component
public class ResendVerificationService implements ResendVerificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResendVerificationService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final EmailSendQuotaPort emailSendQuotaPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public ResendVerificationService(UserRepository userRepository,
                                      VerificationTokenRepository verificationTokenRepository,
                                      EmailSenderPort emailSenderPort,
                                      EmailSendQuotaPort emailSendQuotaPort,
                                      VerificationTokenGenerator tokenGenerator,
                                      VerificationEmailComposer emailComposer,
                                      Clock clock,
                                      @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.emailSendQuotaPort = emailSendQuotaPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public void resend(ResendVerificationCommand command) {
        // Avant toute lecture, comme pour la réinitialisation de mot de passe :
        // un plafond qui ne frapperait que les adresses connues dirait lesquelles
        // le sont.
        emailSendQuotaPort.requireQuota(command.email().value());
        // Split from the old single filter+ifPresent so the two silent causes can be
        // told apart in the logs: an address nobody owns, versus an account that is
        // already verified and therefore needs nothing resent.
        var account = userRepository.findByEmail(command.email().value());
        if (account.isEmpty()) {
            log.info("Verification resend requested for an unknown address ({}) - no email sent",
                    command.email().value());
            return;
        }
        if (account.get().isVerified()) {
            log.info("Verification resend requested for an already-verified account ({}) - no email sent",
                    command.email().value());
            return;
        }
        User user = account.get();
        verificationTokenRepository.deleteByUserId(user.getId());
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(user.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);
        // No returnTo here: unlike RegisterUserService, this path has no request
        // context to carry one from, and no frontend "resend" UI exists yet to
        // supply one - a resend after an invitation-wizard registration drops
        // back to the generic post-verify page instead of resuming the wizard.
        // Accepted gap; revisit if/when a resend UI ships.
        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken, null));
    }
}
