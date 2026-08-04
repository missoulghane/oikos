package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.PartyDetails;
import com.architek.oikos.invitation.application.port.out.PartyDirectoryPort;
import com.architek.oikos.invitation.application.port.out.UnitBasicInfo;
import com.architek.oikos.invitation.application.port.out.UnitDirectoryPort;
import com.architek.oikos.invitation.domain.exception.InvalidInvitationTokenException;
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
 * PUBLIC invitations are reusable and never consumed by a submission - only
 * the request itself goes PENDING; the invitation's own status is untouched
 * (contrast with AcceptInvitationService, which consumes a single-use
 * PRIVATE_* invitation). No unit lock is taken here: per product decision, a
 * unit stays selectable by other candidates while requests are pending -
 * only AcceptMembershipRequestService claims it, atomically, at decision time.
 */
@Component
public class SubmitMembershipRequestService implements SubmitMembershipRequestUseCase {

    private final InvitationRepository invitationRepository;
    private final MembershipRequestRepository membershipRequestRepository;
    private final PartyDirectoryPort partyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final AccountDirectoryPort accountDirectoryPort;
    private final Clock clock;

    public SubmitMembershipRequestService(InvitationRepository invitationRepository,
                                           MembershipRequestRepository membershipRequestRepository,
                                           PartyDirectoryPort partyDirectoryPort, UnitDirectoryPort unitDirectoryPort,
                                           AccountDirectoryPort accountDirectoryPort, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.membershipRequestRepository = membershipRequestRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MembershipRequestId submit(SubmitMembershipRequestCommand command) {
        Invitation invitation = invitationRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidInvitationTokenException("Invalid invitation link"));
        if (!invitation.isUsable(clock.instant())) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (invitation.getType() != InvitationType.PUBLIC) {
            throw new InvalidInvitationTokenException("This invitation link is no longer usable");
        }
        if (command.unitId() == null) {
            throw new IllegalArgumentException("unitId is required to submit a candidacy");
        }
        UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(command.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found with id: " + command.unitId()));
        if (!unit.propertyId().equals(invitation.getPropertyId())) {
            throw new IllegalArgumentException("Unit " + command.unitId() + " does not belong to this invitation's property");
        }

        EntityId userId;
        EntityId partyId;
        if (command.actingUserId() != null) {
            AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(command.actingUserId());
            userId = command.actingUserId();
            partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());
        } else {
            if (command.email() == null || command.fullName() == null || command.password() == null) {
                throw new IllegalArgumentException("email, fullName and password are required to submit a candidacy anonymously");
            }
            userId = accountDirectoryPort.provisionAccount(command.email(), command.fullName(), command.password());
            partyId = resolveParty(command.email(), command.fullName(), invitation.getPropertyId());
        }

        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitation.getId().asUuid()), invitation.getPropertyId(), command.unitId(), partyId, userId);
        return membershipRequestRepository.save(request).getId();
    }

    private EntityId resolveParty(EmailVO email, String fullName, EntityId propertyId) {
        return partyDirectoryPort.findIdByEmail(email, propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(new PartyDetails(fullName, PartyType.INDIVIDUAL, email, null), propertyId));
    }
}
