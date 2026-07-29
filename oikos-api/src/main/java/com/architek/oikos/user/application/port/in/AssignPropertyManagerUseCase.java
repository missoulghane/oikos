package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.AssignPropertyManagerCommand;

/**
 * ADMIN-only: assigns an already-registered account as manager of an
 * existing property, by email. A trusted admin action (no consent flow,
 * unlike InvitePartyUseCase) - the target account must already exist; if
 * not, the admin should invite them to register first.
 */
public interface AssignPropertyManagerUseCase {

    void assign(AssignPropertyManagerCommand command);
}
