package com.architek.oikos.invitation.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Same authenticated-only shape as AcceptInvitationCommand - see its doc for
 * why the anonymous email/fullName/password branch was removed.
 */
public record SubmitMembershipRequestCommand(String token, EntityId actingUserId, EntityId unitId) {
}
