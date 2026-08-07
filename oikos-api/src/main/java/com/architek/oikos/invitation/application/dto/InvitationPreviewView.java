package com.architek.oikos.invitation.application.dto;

import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Public, pre-authentication view of an invitation: usable/reason let the
 * landing page render "this link has expired/been disabled" cleanly instead
 * of a hard error. Neither remaining type (PUBLIC, PRIVATE) fixes a unit at
 * invitation time, so there is no unit to preview here - the picker is
 * always shown, populated from the available-units endpoint.
 */
public record InvitationPreviewView(InvitationType type, boolean usable, String reason, String propertyName,
                                     String propertyAddress, EmailVO targetEmail, String boardRole) {
}
