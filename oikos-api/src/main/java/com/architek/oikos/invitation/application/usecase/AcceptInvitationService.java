package com.architek.oikos.invitation.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.invitation.application.command.AcceptInvitationCommand;
import com.architek.oikos.invitation.application.port.in.AcceptInvitationUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
import com.architek.oikos.invitation.domain.exception.UnitUnavailableException;
import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.repository.InvitationRepository;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;

/**
 * Handles PRIVATE_WITH_UNIT (unit fixed by the invitation) and
 * PRIVATE_WITHOUT_UNIT (unit chosen by the caller, validated against this
 * invitation's own property). PUBLIC never reaches this use case - it goes
 * through SubmitMembershipRequestUseCase instead, since it needs manager
 * review.
 */
@Component
public class AcceptInvitationService implements AcceptInvitationUseCase {

    private final InvitationRepository invitationRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final Clock clock;

    public AcceptInvitationService(InvitationRepository invitationRepository, PartyDirectoryPort partyDirectoryPort,
                                    UnitDirectoryPort unitDirectoryPort, AccountDirectoryPort accountDirectoryPort,
                                    Clock clock) {
        this.invitationRepository = invitationRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
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
        if (invitation.getType() != InvitationType.PRIVATE_WITH_UNIT && invitation.getType() != InvitationType.PRIVATE_WITHOUT_UNIT) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        EntityId unitId = resolveUnitId(invitation, command.unitId());

        EntityId userId;
        EntityId partyId;
        if (command.actingUserId() != null) {
            AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(command.actingUserId());
            userId = command.actingUserId();
            partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());
        } else {
            if (command.email() == null || command.fullName() == null || command.password() == null) {
                throw new IllegalArgumentException("email, fullName and password are required to accept an invitation anonymously");
            }
            userId = accountDirectoryPort.provisionAccount(command.email(), command.fullName(), command.password());
            partyId = resolveParty(command.email(), command.fullName(), invitation.getPropertyId());
        }

        try {
            unitDirectoryPort.claim(unitId, partyId);
        } catch (UnitUnavailableException e) {
            invitationRepository.save(invitation.disable());
            throw e;
        }

        accountDirectoryPort.grantPropertyRole(userId, partyId, invitation.getPropertyId(), invitation.getTargetRole());
        invitationRepository.save(invitation.consume());
        return userId;
    }

    /**
     * PRIVATE_WITH_UNIT's unit is fixed by the invitation - never taken from
     * client input. PRIVATE_WITHOUT_UNIT's unit is client-supplied, so it
     * must be re-validated as actually belonging to this invitation's
     * property (never trust a client-submitted id from a different
     * property); availability itself is enforced atomically by the claim
     * below, not here.
     */
    private EntityId resolveUnitId(Invitation invitation, EntityId requestedUnitId) {
        if (invitation.getType() == InvitationType.PRIVATE_WITH_UNIT) {
            return invitation.getUnitId();
        }
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
}
