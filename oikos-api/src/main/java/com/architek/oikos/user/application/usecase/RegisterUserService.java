package com.architek.oikos.user.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.application.port.out.EmailSenderPort;
import com.architek.oikos.shared.application.port.out.PasswordEncoderPort;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.command.RegisterUserCommand;
import com.architek.oikos.user.application.port.in.RegisterUserUseCase;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.exception.RoleNotAllowedException;
import com.architek.oikos.user.domain.model.RegistrableRoles;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

@Component
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterUserService(UserRepository userRepository,
                                VerificationTokenRepository verificationTokenRepository,
                                PasswordEncoderPort passwordEncoderPort,
                                EmailSenderPort emailSenderPort,
                                VerificationTokenGenerator tokenGenerator,
                                VerificationEmailComposer emailComposer,
                                Clock clock,
                                @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
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
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        Role role = command.role() != null ? command.role() : Role.ROLE_USER;
        if (!RegistrableRoles.isAllowed(role)) {
            throw new RoleNotAllowedException(role);
        }
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), hashedPassword, role);
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        return savedUser.getId();
    }
}
