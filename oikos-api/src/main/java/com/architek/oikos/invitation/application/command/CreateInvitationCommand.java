package com.architek.oikos.invitation.application.command;

import com.architek.oikos.invitation.domain.model.InvitationType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateInvitationCommand(EntityId propertyId, InvitationType type, EntityId unitId, EmailVO targetEmail,
                                       EntityId createdByUserId) {
}
