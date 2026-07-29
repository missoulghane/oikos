package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.GrantCreatorAsManagerCommand;

/**
 * Grants the creator of a newly-created property (an already-authenticated
 * AppUser, e.g. via POST /properties) a property-scoped Party and
 * ROLE_PROPERTY_MANAGER grant on it - the "already logged in" counterpart to
 * RegisterPropertyManagerService's public self-registration bootstrap flow,
 * which creates the account and the property together.
 */
public interface GrantCreatorAsManagerUseCase {

    void grant(GrantCreatorAsManagerCommand command);
}
