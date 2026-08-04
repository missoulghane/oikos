package com.architek.oikos.invitation.web.response;

import java.time.Instant;

import com.architek.oikos.invitation.application.dto.MembershipRequestView;
import com.architek.oikos.invitation.domain.model.MembershipRequestStatus;

public record MembershipRequestResponse(String id, String invitationId, String propertyId, String unitId,
                                         String partyId, MembershipRequestStatus status, Instant decidedAt,
                                         String decidedByUserId, String rejectionReason) {

    public static MembershipRequestResponse from(MembershipRequestView view) {
        return new MembershipRequestResponse(view.id().toString(), view.invitationId().toString(),
                view.propertyId().toString(), view.unitId().toString(), view.partyId().toString(), view.status(),
                view.decidedAt(), view.decidedByUserId() != null ? view.decidedByUserId().toString() : null,
                view.rejectionReason());
    }
}
