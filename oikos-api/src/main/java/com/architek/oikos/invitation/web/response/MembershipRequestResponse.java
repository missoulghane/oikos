package com.architek.oikos.invitation.web.response;

import java.time.Instant;

import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewStatus;
import com.architek.oikos.invitation.application.dto.MembershipRequestOverviewView;

/**
 * id is a MembershipRequestId for a real request (PENDING/ACCEPTED/REJECTED)
 * but the owning Invitation's own id for a synthetic INVITED row - see
 * MembershipRequestOverviewView. unitId/partyId are null for INVITED rows
 * (no unit chosen, no account created yet); targetEmail is null for every
 * other status (the party already carries the email once one exists).
 */
public record MembershipRequestResponse(String id, String invitationId, String propertyId, String unitId,
                                         String partyId, String targetEmail, MembershipRequestOverviewStatus status,
                                         Instant decidedAt, String decidedByUserId, String rejectionReason) {

    public static MembershipRequestResponse from(MembershipRequestOverviewView view) {
        return new MembershipRequestResponse(view.id().toString(), view.invitationId().toString(),
                view.propertyId().toString(), view.unitId() != null ? view.unitId().toString() : null,
                view.partyId() != null ? view.partyId().toString() : null,
                view.targetEmail() != null ? view.targetEmail().value() : null, view.status(), view.decidedAt(),
                view.decidedByUserId() != null ? view.decidedByUserId().toString() : null, view.rejectionReason());
    }
}
