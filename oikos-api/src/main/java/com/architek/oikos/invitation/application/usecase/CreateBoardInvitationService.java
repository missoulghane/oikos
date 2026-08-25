package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.CreateBoardInvitationCommand;
import com.architek.oikos.invitation.application.port.in.CreateBoardInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.service.InvitationTokenGenerator;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * Board invitations are always PRIVATE with a mandatory targetEmail - unlike
 * CreateInvitationService's PUBLIC case (unit picked from an open pool), a
 * board seat is always offered to one specific person for one specific role,
 * so there is no "no target" variant to validate against here. Pas de lot non
 * plus (targetUnitId nul) : un siège au conseil n'en est pas un.
 */
@Component
public class CreateBoardInvitationService implements CreateBoardInvitationUseCase {

    private static final String TARGET_ROLE_BOARD_MEMBER = "PROPERTY_BOARD_MEMBER";

    private final InvitationRepository invitationRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final InvitationTokenGenerator tokenGenerator;
    private final Clock clock;
    private final Duration invitationTokenTtl;

    public CreateBoardInvitationService(InvitationRepository invitationRepository, PropertyDirectoryPort propertyDirectoryPort,
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
    public InvitationId create(CreateBoardInvitationCommand command) {
        propertyDirectoryPort.findBasicInfo(command.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + command.propertyId()));

        String rawToken = tokenGenerator.generate();
        Instant expiresAt = clock.instant().plus(invitationTokenTtl);
        Invitation invitation = Invitation.issue(InvitationId.newId(), command.propertyId(), InvitationType.PRIVATE,
                TARGET_ROLE_BOARD_MEMBER, command.targetEmail(), rawToken, expiresAt, command.createdByUserId(),
                command.boardRole(), null, null);

        return invitationRepository.save(invitation).getId();
    }
}
