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
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.application.port.out.ContactDetails;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;
import com.architek.oikos.user.domain.exception.LoginAlreadyUsedException;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final ContactDirectoryPort contactDirectoryPort;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterUserService(UserRepository userRepository,
                                ContactDirectoryPort contactDirectoryPort,
                                VerificationTokenRepository verificationTokenRepository,
                                PasswordEncoderPort passwordEncoderPort,
                                EmailSenderPort emailSenderPort,
                                VerificationTokenGenerator tokenGenerator,
                                VerificationEmailComposer emailComposer,
                                Clock clock,
                                @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.contactDirectoryPort = contactDirectoryPort;
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
    public UserId register(RegisterUserCommand command) {
        if (command.login() != null && !command.login().isBlank() && userRepository.existsByLogin(command.login())) {
            throw new LoginAlreadyUsedException(command.login());
        }
        EntityId contactId = contactDirectoryPort.createContact(
                new ContactDetails(command.lastName(), command.firstName(), command.email(), command.phone()));
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), contactId, hashedPassword, command.login());
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        return savedUser.getId();
    }
}
