package com.architek.oikos.invitation.application.dto;

import java.time.Instant;

import com.architek.oikos.invitation.domain.model.MembershipRequest;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;
import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record MembershipRequestView(MembershipRequestId id, EntityId invitationId, EntityId propertyId, EntityId unitId,
                                     EntityId partyId, EntityId userId, MembershipRequestStatus status,
                                     Instant decidedAt, EntityId decidedByUserId, String rejectionReason) {

    public static MembershipRequestView from(MembershipRequest request) {
        return new MembershipRequestView(request.getId(), request.getInvitationId(), request.getPropertyId(),
                request.getUnitId(), request.getPartyId(), request.getUserId(), request.getStatus(),
                request.getDecidedAt(), request.getDecidedByUserId(), request.getRejectionReason());
    }
}
