package com.architek.oikos.invitation.application.command;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The caller must always be authenticated by this point - account creation
 * now happens upstream via the standard registration flow, never inline
 * here (see PublicInvitationController's @PreAuthorize). unitId is the lot
 * the invitee chose, re-validated against this invitation's property in
 * AcceptInvitationService.
 */
public record AcceptInvitationCommand(String token, EntityId actingUserId, EntityId unitId) {
}
