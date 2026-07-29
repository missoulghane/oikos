package com.architek.oikos.user.application.command;

/**
 * password is required only when accepting the invitation creates a brand
 * new AppUser (no account exists yet for the invited email) - ignored when
 * linking to an already-existing account.
 */
public record AcceptPartyInvitationCommand(String token, String password) {
}
