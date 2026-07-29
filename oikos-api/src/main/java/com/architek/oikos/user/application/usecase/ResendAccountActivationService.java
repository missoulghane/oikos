package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.user.application.command.ResendAccountActivationCommand;
import com.architek.oikos.user.application.port.in.ResendAccountActivationUseCase;
import com.architek.oikos.user.domain.exception.AccountAlreadyVerifiedException;
import com.architek.oikos.user.domain.exception.UserNotFoundException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;

/**
 * Admin-triggered resend of the activation email (e.g. the original link
 * expired or was lost). Unlike the public self-registration resend, this is
 * explicit and targeted by id, so it is not silent about the account's state:
 * the admin already sees it in their own user list.
 */
@Component
public class ResendAccountActivationService implements ResendAccountActivationUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final AccountActivationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public ResendAccountActivationService(UserRepository userRepository,
                                           VerificationTokenRepository verificationTokenRepository,
                                           EmailSenderPort emailSenderPort,
                                           VerificationTokenGenerator tokenGenerator,
                                           AccountActivationEmailComposer emailComposer,
                                           Clock clock,
                                           @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public void resend(ResendAccountActivationCommand command) {
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));
        if (user.isVerified()) {
            throw new AccountAlreadyVerifiedException();
        }
        verificationTokenRepository.deleteByUserId(user.getId());
        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken activationToken = VerificationToken.issue(user.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(activationToken);
        emailSenderPort.send(user.getEmail(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
    }
}
