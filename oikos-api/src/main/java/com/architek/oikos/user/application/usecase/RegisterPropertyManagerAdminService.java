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
import com.architek.oikos.user.application.command.RegisterPropertyManagerAdminCommand;
import com.architek.oikos.user.application.port.in.RegisterPropertyManagerAdminUseCase;
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
 * Registers a professional property-management firm's admin account:
 * creates the property they administer (without a building - added later),
 * then a Party scoped to that property, assigns it as the property's
 * PROPERTY_MANAGER board member, and finally the User account (ROLE_USER
 * globally, PROPERTY_MANAGER_ADMIN granted through the linked Party) - in
 * that order, since the Party can only be created once its property id is
 * known. Unlike RegisterPropertyBoardAdminService, this account is
 * uncapped: it may create any number of additional properties afterwards
 * via the authenticated POST /properties endpoint. Issues a verification
 * token and sends the verification email afterwards - same activation flow
 * as a plain user registration (see RegisterUserService).
 */
@Component
public class RegisterPropertyManagerAdminService implements RegisterPropertyManagerAdminUseCase {

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

    public RegisterPropertyManagerAdminService(UserRepository userRepository,
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
    public UserId register(RegisterPropertyManagerAdminCommand command) {
        if (userRepository.existsByEmail(command.email().value())) {
            throw new EmailAlreadyUsedException(command.email().value());
        }
        EntityId propertyId = propertyProvisioningPort.provisionProperty(
                new PropertyProvisioningDetails(command.propertyName(), command.propertyAddress(), command.propertyCity()));
        EntityId partyId = partyProvisioningPort.createParty(
                new PartyProvisioningDetails(command.fullName(), command.email(), command.phone(), propertyId));
        propertyProvisioningPort.assignPropertyManager(propertyId, partyId);

        HashedPassword hashedPassword = passwordEncoderPort.encode(command.password());
        User user = User.register(UserId.newId(), command.email(), command.fullName(), command.phone(), hashedPassword,
                        Role.ROLE_USER)
                .withLinkedParty(partyId)
                .withPropertyRoleGrant(partyId, propertyId, PropertyRole.PROPERTY_MANAGER_ADMIN);
        User savedUser = userRepository.save(user);

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(verificationTokenTtl);
        VerificationToken verificationToken = VerificationToken.issue(savedUser.getId(), rawToken, expiresAt);
        verificationTokenRepository.save(verificationToken);

        emailSenderPort.send(command.email(), emailComposer.subject(), emailComposer.htmlBody(rawToken, null));
        return savedUser.getId();
    }
}
