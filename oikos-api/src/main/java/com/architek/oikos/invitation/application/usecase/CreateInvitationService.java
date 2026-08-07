package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.CreateInvitationCommand;
import com.architek.oikos.invitation.application.port.in.CreateInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * targetRole is hardcoded to PROPERTY_OWNER for now - the domain model keeps
 * it as a plain field (not a constant) so board-member invitations can reuse
 * this same use case later without a schema change, but nothing today lets a
 * caller choose a different role.
 */
@Component
public class CreateInvitationService implements CreateInvitationUseCase {

    private static final String TARGET_ROLE_OWNER = "PROPERTY_OWNER";

    private final InvitationRepository invitationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final InvitationTokenGenerator tokenGenerator;
    private final Clock clock;
    private final Duration invitationTokenTtl;

    public CreateInvitationService(InvitationRepository invitationRepository, PropertyDirectoryPort propertyDirectoryPort,
                                    InvitationTokenGenerator tokenGenerator, Clock clock,
                                    @Value("${oikos.mail.invitation-token-ttl-days}") long invitationTokenTtlDays) {
        this.invitationRepository = invitationRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
        this.invitationTokenTtl = Duration.ofDays(invitationTokenTtlDays);
    }

    @Override
    @Transactional
    public InvitationId create(CreateInvitationCommand command) {
        propertyDirectoryPort.findBasicInfo(command.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + command.propertyId()));

        if (command.type() != InvitationType.PUBLIC && command.targetEmail() == null) {
            throw new IllegalArgumentException("targetEmail is required for " + command.type() + " invitations");
        }

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(invitationTokenTtl);
        Invitation invitation = Invitation.issue(InvitationId.newId(), command.propertyId(), command.type(),
                TARGET_ROLE_OWNER, command.targetEmail(), rawToken, expiresAt, command.createdByUserId(), null);

        return invitationRepository.save(invitation).getId();
    }
}
