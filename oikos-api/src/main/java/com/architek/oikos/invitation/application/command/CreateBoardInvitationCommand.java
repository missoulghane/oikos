package com.architek.oikos.invitation.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record CreateBoardInvitationCommand(EntityId propertyId, EmailVO targetEmail, String boardRole,
                                            EntityId createdByUserId) {
}
