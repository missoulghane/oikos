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
import com.architek.oikos.user.application.port.out.PartyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PartyProvisioningPort;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;
import com.architek.oikos.user.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.model.Role;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.repository.VerificationTokenRepository;
import com.architek.oikos.user.domain.service.VerificationTokenGenerator;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Registers a property manager: creates the property they manage (without a
 * building - the manager adds buildings later), then a Party scoped to that
 * property, assigns it as the property's PROPERTY_MANAGER board member, and
 * finally the User account (ROLE_USER globally, ROLE_PROPERTY_MANAGER
 * granted through the linked Party) - in that order, since the Party can
 * only be created once its property id is known. Issues a verification
 * token and sends the verification email afterwards - same activation flow
 * as a plain user registration (see RegisterUserService).
 */
@Component
public class RegisterPropertyManagerService implements RegisterPropertyManagerUseCase {

    private final UserRepository userRepository;
    private final PartyProvisioningPort partyProvisioningPort;
    private final PropertyProvisioningPort propertyProvisioningPort;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final EmailSenderPort emailSenderPort;
    private final VerificationTokenGenerator tokenGenerator;
    private final VerificationEmailComposer emailComposer;
    private final Clock clock;
    private final Duration verificationTokenTtl;

    public RegisterPropertyManagerService(UserRepository userRepository,
                                           PartyProvisioningPort partyProvisioningPort,
                                           PropertyProvisioningPort propertyProvisioningPort,
                                           VerificationTokenRepository verificationTokenRepository,
                                           PasswordEncoderPort passwordEncoderPort,
                                           EmailSenderPort emailSenderPort,
                                           VerificationTokenGenerator tokenGenerator,
                                           VerificationEmailComposer emailComposer,
                                           Clock clock,
                                           @Value("${oikos.mail.verification-token-ttl-hours}") long verificationTokenTtlHours) {
        this.userRepository = userRepository;
        this.partyProvisioningPort = partyProvisioningPort;
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
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        EntityId propertyId = propertyProvisioningPort.provisionProperty(
                new PropertyProvisioningDetails(command.propertyName(), command.propertyAddress()));
        EntityId partyId = partyProvisioningPort.createParty(
                new PartyProvisioningDetails(command.fullName(), command.email(), command.phone(), propertyId));
        propertyProvisioningPort.assignPropertyManager(propertyId, partyId);

        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), hashedPassword, Role.ROLE_USER)
                .withLinkedParty(partyId)
                .withPropertyRoleGrant(partyId, propertyId, PropertyRole.ROLE_PROPERTY_MANAGER);
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken));
        return savedUser.getId();
    }
}
