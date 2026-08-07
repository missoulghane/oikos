package com.architek.oikos.invitation.application.dto;

import java.time.Instant;

import com.architek.oikos.invitation.domain.model.Invitation;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.invitation.domain.valueobject.InvitationId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InvitationView(InvitationId id, EntityId propertyId, InvitationType type, String targetRole,
                              EmailVO targetEmail, String link, InvitationStatus status, Instant expiresAt,
                              EntityId createdByUserId, boolean emailMismatch, String boardRole) {

    public static InvitationView from(Invitation invitation, String link) {
        return new InvitationView(invitation.getId(), invitation.getPropertyId(), invitation.getType(),
                invitation.getTargetRole(), invitation.getTargetEmail(), link, invitation.getStatus(),
                invitation.getExpiresAt(), invitation.getCreatedByUserId(), invitation.emailMismatch(),
                invitation.getTargetBoardRole());
    }
}
