package com.architek.oikos.user.application.port.in;

import java.util.Optional;

import com.architek.oikos.user.domain.model.User;

/**
 * Public entry point used by the auth feature (infrastructure/security layer) to load
 * account credentials during authentication. The identifier can be the account's own
 * login, or the email/phone of its linked party (checked in that priority order -
 * see LoadUserByIdentifierService). Cross-feature access must go through a port-in
 * use case, never through the user repository directly.
 */
public interface LoadUserByIdentifierUseCase {

    Optional<User> loadByIdentifier(String identifier);
}
