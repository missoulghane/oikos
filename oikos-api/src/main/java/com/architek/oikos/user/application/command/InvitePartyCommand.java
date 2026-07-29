package com.architek.oikos.user.application.command;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InvitePartyCommand(EntityId partyId, EmailVO email, String fullName) {
}
