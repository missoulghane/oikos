package com.architek.oikos.invitation.application.dto;

import java.time.Instant;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Manager-facing "Demandes d'adhésion" overview: merges real, persisted
 * MembershipRequest rows (PUBLIC candidates, always reviewed by a manager)
 * with still-outstanding PRIVATE invitations (auto-accepted, no manager
 * review - see ListMembershipRequestsService) into one unified shape, so the
 * manager doesn't have to cross-reference two separate lists to track who
 * has and hasn't joined the property.
 *
 * <p>fromInvitation(...) produces a synthetic INVITED entry: unitId/partyId
 * are null (no unit chosen, no account created yet), targetEmail carries the
 * invitation's own target instead. It is never persisted - see
 * MembershipRequestOverviewStatus's Javadoc.
 */
public record MembershipRequestOverviewView(EntityId id, EntityId invitationId, EntityId propertyId, EntityId unitId,
                                             EntityId partyId, EmailVO targetEmail, MembershipRequestOverviewStatus status,
                                             Instant decidedAt, EntityId decidedByUserId, String rejectionReason) {

    public static MembershipRequestOverviewView fromRequest(MembershipRequest request) {
        return new MembershipRequestOverviewView(EntityId.of(request.getId().asUuid()), request.getInvitationId(),
                request.getPropertyId(), request.getUnitId(), request.getPartyId(), null,
                MembershipRequestOverviewStatus.valueOf(request.getStatus().name()), request.getDecidedAt(),
                request.getDecidedByUserId(), request.getRejectionReason());
    }

    public static MembershipRequestOverviewView fromInvitation(Invitation invitation) {
        return new MembershipRequestOverviewView(EntityId.of(invitation.getId().asUuid()),
                EntityId.of(invitation.getId().asUuid()), invitation.getPropertyId(), null, null,
                invitation.getTargetEmail(), MembershipRequestOverviewStatus.INVITED, null, null, null);
    }
}
