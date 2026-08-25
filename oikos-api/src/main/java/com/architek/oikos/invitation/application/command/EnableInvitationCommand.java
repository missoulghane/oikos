package com.architek.oikos.invitation.application.command;

import com.architek.oikos.invitation.domain.valueobject.InvitationId;

public record EnableInvitationCommand(InvitationId id) {
}
