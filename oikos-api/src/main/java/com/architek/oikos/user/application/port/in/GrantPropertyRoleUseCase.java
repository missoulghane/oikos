package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.command.GrantPropertyRoleCommand;

/**
 * Purely additive: links the party to the user's account (if not already
 * linked) and grants the property-scoped role. Unlike
 * GrantCreatorAsManagerService, never seats the party on the property's
 * board - that's a manager/board-admin-only concept, not applicable to the
 * PROPERTY_OWNER grants this is used for today.
 */
public interface GrantPropertyRoleUseCase {

    void grant(GrantPropertyRoleCommand command);
}
