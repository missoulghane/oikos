package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.InvitePartyCommand;

/**
 * Public entry point used by other features (e.g. property, right after
 * resolving/creating the Party behind a unit owner) to invite that party's
 * email to link an AppUser account to it, and by the party feature's own
 * "resend invitation" action. A no-op if the party is already linked to an
 * account. Cross-feature access must go through this port-in use case, never
 * through the user repository directly (rule 6).
 *
 * @return true if an invitation email was (re-)sent, false if the party was
 * already linked to an account (no-op)
 */
public interface InvitePartyUseCase {

    boolean invite(InvitePartyCommand command);
}
