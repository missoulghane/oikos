package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.port.in.AcceptInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardDirectoryPort;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.invitation.domain.repository.MembershipRequestRepository;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Handles PRIVATE invitations. Two shapes share this same token/usability
 * validation but then fork: board invitations (targetRole
 * PROPERTY_BOARD_MEMBER) attach the party to the board as a PENDING_
 * VALIDATION seat, with no unit to claim and no MembershipRequest audit row
 * (that trail is unit-ownership-specific) - accepting the link only
 * consumes the invitation, it does not grant the PROPERTY_BOARD_MEMBER role
 * by itself; an admin must explicitly validate the seat afterwards (see
 * property.application.usecase.ValidateBoardMemberService) for the role to
 * actually be granted. Everything else is the owner flow, where the unit is
 * always chosen by the caller and re-validated as belonging to this
 * invitation's property (never trust a client-submitted id from a different
 * property) - availability itself is enforced atomically by the claim below,
 * and the PROPERTY_OWNER role is granted immediately there.
 * PUBLIC never reaches this use case - it goes through
 * SubmitMembershipRequestUseCase instead, since it needs manager review.
 */
@Component
public class AcceptInvitationService implements AcceptInvitationUseCase {

    private static final String TARGET_ROLE_BOARD_MEMBER = "PROPERTY_BOARD_MEMBER";

    private final InvitationRepository invitationRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final BoardDirectoryPort boardDirectoryPort;
    private final MembershipRequestRepository membershipRequestRepository;
    private final Clock clock;

    public AcceptInvitationService(InvitationRepository invitationRepository, PartyDirectoryPort partyDirectoryPort,
                                    UnitDirectoryPort unitDirectoryPort, AccountDirectoryPort accountDirectoryPort,
                                    BoardDirectoryPort boardDirectoryPort,
                                    MembershipRequestRepository membershipRequestRepository, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.boardDirectoryPort = boardDirectoryPort;
        this.membershipRequestRepository = membershipRequestRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EntityId accept(AcceptInvitationCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
        if (!invitation.isUsable(clock.instant())) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (invitation.getType() != InvitationType.PRIVATE) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (isBoardInvitation(invitation)) {
            return acceptBoardInvitation(invitation, command.actingUserId());
        }
        EntityId unitId = resolveUnitId(invitation, command.unitId());

        AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(command.actingUserId());
        EntityId userId = command.actingUserId();
        EmailVO resolvedEmail = accountInfo.email();
        EntityId partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());

        try {
            unitDirectoryPort.claim(unitId, partyId);
        } catch (UnitUnavailableException e) {
            invitationRepository.save(invitation.disable());
            throw e;
        }

        accountDirectoryPort.grantPropertyRole(userId, partyId, invitation.getPropertyId(), invitation.getTargetRole());
        invitationRepository.save(invitation.consume(resolvedEmail));

        // PRIVATE never goes through manager review, so this is created
        // directly in ACCEPTED status (submit()+accept() with no PENDING
        // phase in between) - a permanent audit trail row alongside the
        // access just granted, replacing the synthetic INVITED entry the
        // manager's overview showed for this invitation until now (see
        // ListMembershipRequestsService).
        membershipRequestRepository.save(
                MembershipRequest.submit(MembershipRequestId.newId(), EntityId.of(invitation.getId().asUuid()),
                                invitation.getPropertyId(), unitId, partyId, userId)
                        .accept(clock.instant(), null));

        return userId;
    }

    /**
     * The unit is always client-supplied for PRIVATE invitations now (no
     * PRIVATE_WITH_UNIT left to fix it on the invitation itself), so it must
     * be re-validated as actually belonging to this invitation's property
     * (never trust a client-submitted id from a different property);
     * availability itself is enforced atomically by the claim below, not here.
     */
    private EntityId resolveUnitId(Invitation invitation, EntityId requestedUnitId) {
        if (requestedUnitId == null) {
            throw new IllegalArgumentException("unitId is required to accept this invitation");
        }
        UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(requestedUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found with id: " + requestedUnitId));
        if (!unit.propertyId().equals(invitation.getPropertyId())) {
            throw new IllegalArgumentException("Unit " + requestedUnitId + " does not belong to this invitation's property");
        }
        return requestedUnitId;
    }

    private EntityId resolveParty(EmailVO email, String fullName, EntityId propertyId) {
        return partyDirectoryPort.findIdByEmail(email, propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(new PartyDetails(fullName, PartyType.INDIVIDUAL, email, null), propertyId));
    }

    private boolean isBoardInvitation(Invitation invitation) {
        return TARGET_ROLE_BOARD_MEMBER.equals(invitation.getTargetRole());
    }

    /**
     * No lot to claim and no MembershipRequest audit row here - that trail
     * exists to track unit-ownership candidacies, which a board seat isn't.
     * The PROPERTY_BOARD_MEMBER role is NOT granted here: the seat is
     * created PENDING_VALIDATION and only becomes ACTIVE (with the role
     * actually granted) once an admin validates it. Accepting the link only
     * consumes the invitation.
     */
    private EntityId acceptBoardInvitation(Invitation invitation, EntityId actingUserId) {
        AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(actingUserId);
        EntityId partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());

        boardDirectoryPort.addPendingBoardMember(invitation.getPropertyId(), partyId, actingUserId, invitation.getTargetBoardRole());
        invitationRepository.save(invitation.consume(accountInfo.email()));

        return actingUserId;
    }
}
