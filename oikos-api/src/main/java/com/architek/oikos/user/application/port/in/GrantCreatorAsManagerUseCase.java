package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.GrantCreatorAsManagerCommand;

/**
 * Grants the creator of a newly-created property (an already-authenticated
 * AppUser, e.g. via POST /properties) a property-scoped Party and the given
 * PropertyRole grant on it - the "already logged in" counterpart to
 * RegisterPropertyBoardAdminService/RegisterPropertyManagerAdminService's
 * public self-registration bootstrap flows, which create the account and
 * the property together. Also used to invite a MEMBER-tier account onto an
 * existing property (see AssignPropertyManagerService).
 */
public interface GrantCreatorAsManagerUseCase {

    void grant(GrantCreatorAsManagerCommand command);
}
