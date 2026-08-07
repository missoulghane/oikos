package com.architek.oikos.invitation.web.request;

/**
 * unitId is the lot the invitee chose - the only thing the wizard's final
 * step still needs to send, now that account creation happens upstream via
 * the standard registration flow (see PublicInvitationController).
 */
public record AcceptInvitationRequest(String unitId) {
}
