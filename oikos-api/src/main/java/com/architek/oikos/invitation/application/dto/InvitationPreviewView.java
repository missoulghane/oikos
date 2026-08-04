package com.architek.oikos.invitation.application.dto;

import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Public, pre-authentication view of an invitation: usable/reason let the
 * landing page render "this link has expired/been disabled" cleanly instead
 * of a hard error, and unit fields are only populated for PRIVATE_WITH_UNIT.
 */
public record InvitationPreviewView(InvitationType type, boolean usable, String reason, String propertyName,
                                     String propertyAddress, String unitNumber, String unitTypeName,
                                     EmailVO targetEmail) {
}
