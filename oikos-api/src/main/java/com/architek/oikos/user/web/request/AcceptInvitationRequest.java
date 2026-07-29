package com.architek.oikos.user.web.request;

import jakarta.validation.constraints.NotBlank;

/**
 * password is optional: only required when the invited email has no
 * existing account yet (AcceptPartyInvitationService enforces this - a
 * bean-validation constraint here can't express "required only sometimes").
 */
public record AcceptInvitationRequest(@NotBlank String token, String password) {
}
