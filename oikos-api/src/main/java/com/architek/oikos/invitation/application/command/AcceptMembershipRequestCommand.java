package com.architek.oikos.invitation.application.command;

import com.architek.oikos.invitation.domain.valueobject.MembershipRequestId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AcceptMembershipRequestCommand(MembershipRequestId id, EntityId decidedByUserId) {
}
