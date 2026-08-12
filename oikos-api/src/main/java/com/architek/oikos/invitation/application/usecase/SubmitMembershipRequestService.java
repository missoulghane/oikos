package com.architek.oikos.invitation.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;

import com.architek.oikos.invitation.application.command.SubmitMembershipRequestCommand;
import com.architek.oikos.invitation.application.port.in.SubmitMembershipRequestUseCase;
import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.invitation.application.port.out.BoardStaffDirectoryPort;
import com.architek.oikos.invitation.application.port.out.NotificationPort;
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
    private final BoardStaffDirectoryPort boardStaffDirectoryPort;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public SubmitMembershipRequestService(InvitationRepository invitationRepository,
                                           MembershipRequestRepository membershipRequestRepository,
                                           PartyDirectoryPort partyDirectoryPort, UnitDirectoryPort unitDirectoryPort,
                                           AccountDirectoryPort accountDirectoryPort,
                                           BoardStaffDirectoryPort boardStaffDirectoryPort,
                                           NotificationPort notificationPort, Clock clock) {
        this.invitationRepository = invitationRepository;
        this.membershipRequestRepository = membershipRequestRepository;
        this.partyDirectoryPort = partyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.accountDirectoryPort = accountDirectoryPort;
        this.boardStaffDirectoryPort = boardStaffDirectoryPort;
        this.notificationPort = notificationPort;
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
            throw new IllegalArgumentException("unitId is required to submit a membership request");
        }
        UnitBasicInfo unit = unitDirectoryPort.findBasicInfo(command.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found with id: " + command.unitId()));
        if (!unit.propertyId().equals(invitation.getPropertyId())) {
            throw new IllegalArgumentException("Unit " + command.unitId() + " does not belong to this invitation's property");
        }

        EntityId invitationId = EntityId.of(invitation.getId().asUuid());
        Optional<MembershipRequest> existing =
                membershipRequestRepository.findByInvitationIdAndUnitIdAndUserId(invitationId, command.unitId(), command.actingUserId());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        AccountInfo accountInfo = accountDirectoryPort.getAccountInfo(command.actingUserId());
        EntityId userId = command.actingUserId();
        EntityId partyId = resolveParty(accountInfo.email(), accountInfo.fullName(), invitation.getPropertyId());

        MembershipRequest request = MembershipRequest.submit(MembershipRequestId.newId(),
                EntityId.of(invitation.getId().asUuid()), invitation.getPropertyId(), command.unitId(), partyId, userId);
        MembershipRequestId savedId = membershipRequestRepository.save(request).getId();
        notifyStaff(invitation.getPropertyId(), accountInfo.fullName(), unit.unitNumber());
        return savedId;
    }

    private EntityId resolveParty(EmailVO email, String fullName, EntityId propertyId) {
        return partyDirectoryPort.findIdByEmail(email, propertyId)
                .orElseGet(() -> partyDirectoryPort.createParty(new PartyDetails(fullName, PartyType.INDIVIDUAL, email, null), propertyId));
    }

    /** REQUEST_RECEIVED (GAP.md §3.1): only fired on an actual new request, never on the idempotent-duplicate early return above. */
    private void notifyStaff(EntityId propertyId, String requesterFullName, String unitNumber) {
        String title = "Nouvelle demande d'adhésion";
        String body = requesterFullName + " souhaite rejoindre le lot " + unitNumber + ".";
        String linkPath = "/property-mngt/properties/" + propertyId.value() + "/property/invitations";
        for (EntityId staffUserId : boardStaffDirectoryPort.listStaffUserIds(propertyId)) {
            notificationPort.notifyRequestReceived(staffUserId, propertyId, title, body, linkPath);
        }
    }
}
