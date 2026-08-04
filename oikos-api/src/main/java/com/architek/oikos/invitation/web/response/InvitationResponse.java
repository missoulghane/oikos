package com.architek.oikos.invitation.web.response;

import java.time.Instant;

import com.architek.oikos.invitation.application.dto.InvitationView;
import com.architek.oikos.invitation.domain.model.InvitationStatus;
import com.architek.oikos.invitation.domain.model.InvitationType;

public record InvitationResponse(String id, String propertyId, InvitationType type, String targetRole, String unitId,
                                  String targetEmail, String link, InvitationStatus status, Instant expiresAt,
                                  String createdByUserId) {

    public static InvitationResponse from(InvitationView view) {
        return new InvitationResponse(view.id().toString(), view.propertyId().toString(), view.type(), view.targetRole(),
                view.unitId() != null ? view.unitId().toString() : null,
                view.targetEmail() != null ? view.targetEmail().value() : null,
                view.link(), view.status(), view.expiresAt(), view.createdByUserId().toString());
    }
}
