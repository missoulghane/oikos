package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.shared.domain.valueobject.RawPassword;
import com.architek.oikos.user.application.command.CreateUserCommand;
import com.architek.oikos.user.application.port.in.CreateUserUseCase;
import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.domain.exception.LoginAlreadyUsedException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Admin-triggered account creation: the account starts unverified, with a
 * random, never-disclosed placeholder password, and the invited user receives
 * an activation email through which they choose their own password (see
 * ActivateAccountService) - mirroring the public self-registration flow's use
 * of a single-use VerificationToken.
 */
@Component
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final AccountActivationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public CreateUserService(UserRepository userRepository,
                              PartyDirectoryPort partyDirectoryPort,
                              VerificationTokenRepository verificationTokenRepository,
                              PasswordEncoderPort passwordEncoderPort,
                              EmailSenderPort emailSenderPort,
                              VerificationTokenGenerator tokenGenerator,
                              AccountActivationEmailComposer emailComposer,
                              Clock clock,
                              @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.emailSenderPort = emailSenderPort;
        this.tokenGenerator = tokenGenerator;
        this.emailComposer = emailComposer;
        this.clock = clock;
        this.verificationTokenTtl = Duration.ofHours(verificationTokenTtlHours);
    }

    @Override
    @Transactional
    public UserId create(CreateUserCommand command) {
        if (command.login() != null && !command.login().isBlank() && userRepository.existsByLogin(command.login())) {
            throw new LoginAlreadyUsedException(command.login());
        }
        EntityId partyId = partyDirectoryPort.createParty(
                new PartyDetails(command.fullName(), command.email(), command.phone()));
        HashedPassword placeholderPassword = passwordEncoderPort.encode(RawPassword.of(tokenGenerator.generate()));
        User user = User.registerByAdmin(UserId.newId(), partyId, placeholderPassword, command.login());
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken activationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(activationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        return savedUser.getId();
    }
}
