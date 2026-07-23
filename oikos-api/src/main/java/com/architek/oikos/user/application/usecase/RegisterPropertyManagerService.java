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
import com.architek.oikos.user.application.command.RegisterPropertyManagerCommand;
import com.architek.oikos.user.application.port.in.RegisterPropertyManagerUseCase;
import com.architek.oikos.user.application.port.out.PartyDetails;
import com.architek.oikos.user.application.port.out.PartyDirectoryPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.exception.LoginAlreadyUsedException;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Registers a property manager: creates the Party, User (ROLE_PROPERTY_MANAGER,
 * unverified) and the property they manage (without a building - the manager adds
 * buildings later) in the same transaction, then issues a verification token and
 * sends the verification email - same activation flow as a plain user registration
 * (see RegisterUserService).
 */
@Component
public class RegisterPropertyManagerService implements RegisterPropertyManagerUseCase {

    private final UserRepository userRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final PropertyProvisioningPort propertyProvisioningPort;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterPropertyManagerService(UserRepository userRepository,
                                           PartyDirectoryPort partyDirectoryPort,
                                           PropertyProvisioningPort propertyProvisioningPort,
                                           VerificationTokenRepository verificationTokenRepository,
                                           PasswordEncoderPort passwordEncoderPort,
                                           EmailSenderPort emailSenderPort,
                                           VerificationTokenGenerator tokenGenerator,
                                           VerificationEmailComposer emailComposer,
                                           Clock clock,
                                           @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.propertyProvisioningPort = propertyProvisioningPort;
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
    public UserId register(RegisterPropertyManagerCommand command) {
        if (command.login() != null && !command.login().isBlank() && userRepository.existsByLogin(command.login())) {
            throw new LoginAlreadyUsedException(command.login());
        }
        EntityId partyId = partyDirectoryPort.createParty(
                new PartyDetails(command.fullName(), command.email(), command.phone()));
        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), partyId, hashedPassword, command.login(), Role.ROLE_PROPERTY_MANAGER);
        User savedUser = userRepository.save(user);

        propertyProvisioningPort.provisionProperty(new PropertyProvisioningDetails(
                command.propertyName(), command.propertyAddress(), partyId));

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        return savedUser.getId();
    }
}
