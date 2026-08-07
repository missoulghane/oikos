package com.architek.oikos.invitation.web.response;

import com.architek.oikos.invitation.application.dto.InvitationPreviewView;
import com.architek.oikos.invitation.domain.model.InvitationType;

public record InvitationPreviewResponse(InvitationType type, boolean usable, String reason, String propertyName,
                                         String propertyAddress, String targetEmail, String boardRole) {

    public static InvitationPreviewResponse from(InvitationPreviewView view) {
        return new InvitationPreviewResponse(view.type(), view.usable(), view.reason(), view.propertyName(),
                view.propertyAddress(), view.targetEmail() != null ? view.targetEmail().value() : null, view.boardRole());
    }
}
