package com.architek.oikos.invitation.web.request;

import jakarta.validation.constraints.Email;

/**
 * All fields optional at the transport level: when the request carries a
 * valid session (see PublicInvitationController), email/fullName/password
 * are ignored entirely; otherwise all three are required to provision a new
 * account (validated in AcceptInvitationService, since that's a cross-field
 * rule). unitId is only required for PRIVATE_WITHOUT_UNIT invitations,
 * ignored for PRIVATE_WITH_UNIT.
 */
public record AcceptInvitationRequest(@Email String email, String fullName, String password, String unitId) {
}
