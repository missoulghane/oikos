package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.ProvisionInvitedAccountCommand;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Creates a brand-new, already-verified account for someone consuming an
 * invitation link anonymously. Never links to an existing account - if the
 * email is already taken, the invitation flow must instead ask the user to
 * log in (see the invitation module's public accept/candidacy endpoints,
 * which branch on an already-authenticated request rather than reusing this
 * use case in that case).
 */
public interface ProvisionInvitedAccountUseCase {

    UserId provision(ProvisionInvitedAccountCommand command);
}
