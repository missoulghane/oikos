package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.AsyncEmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.RegisterPropertyBoardAdminCommand;
import com.architek.oikos.user.application.port.in.RegisterPropertyBoardAdminUseCase;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Step 1 of the volunteer-syndic wizard: creates the account (ROLE_USER, no
 * property yet) and records when its holder accepted the terms. The property,
 * the Party scoped to it and the PROPERTY_BOARD_ADMIN grant come at step 2
 * (see CreateBoardAdminPropertyService) - a Party is property-scoped, so it
 * cannot exist before the property has a name.
 *
 * <p>Issues a verification token and sends the verification email - same
 * activation flow as a plain user registration (see RegisterUserService). The
 * send goes through AsyncEmailSenderPort: the SMTP round-trip must not make the
 * visitor wait between two steps of the wizard.
 */
@Component
public class RegisterPropertyBoardAdminService implements RegisterPropertyBoardAdminUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final AsyncEmailSenderPort asyncEmailSender;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterPropertyBoardAdminService(UserRepository userRepository,
                                              VerificationTokenRepository verificationTokenRepository,
                                              PasswordEncoderPort passwordEncoderPort,
                                              AsyncEmailSenderPort asyncEmailSender,
                                              VerificationTokenGenerator tokenGenerator,
                                              VerificationEmailComposer emailComposer,
                                              Clock clock,
                                              @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.asyncEmailSender = asyncEmailSender;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public UserId register(RegisterPropertyBoardAdminCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }

        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), command.phone(), hashedPassword,
                        Role.ROLE_USER)
                .withTermsAcceptedAt(clock.instant());
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        verificationTokenRepository.save(VerificationToken.issue(savedUser.getId(), rawToken, expiresAt));

        asyncEmailSender.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken, null));

        return savedUser.getId();
    }
}
