package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.user.application.command.ResendVerificationCommand;
import com.architek.oikos.user.application.port.in.ResendVerificationUseCase;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
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

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final VerificationTokenRepository verificationTokenRepository;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public ResendVerificationService(UserRepository userRepository,
                                      PartyDirectoryPort partyDirectoryPort,
                                      VerificationTokenRepository verificationTokenRepository,
                                      EmailSenderPort emailSenderPort,
                                      VerificationTokenGenerator tokenGenerator,
                                      VerificationEmailComposer emailComposer,
                                      Clock clock,
                                      @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.verificationTokenRepository = verificationTokenRepository;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public void resend(ResendVerificationCommand command) {
        partyDirectoryPort.findIdByEmail(command.email())
                .flatMap(userRepository::findByPartyId)
                .filter(user -> !user.isVerified())
                .ifPresent(user -> {
                    verificationTokenRepository.deleteByUserId(user.getId());
                    String rawToken = tokenGenerator.generate();
                    Instant expiresAt = clock.instant().plus(verificationTokenTtl);
                    VerificationToken verificationToken = VerificationToken.issue(user.getId(), rawToken, expiresAt);
                    verificationTokenRepository.save(verificationToken);
                    emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
                });
    }
}
