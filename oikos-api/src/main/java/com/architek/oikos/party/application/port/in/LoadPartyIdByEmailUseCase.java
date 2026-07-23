package com.architek.oikos.party.application.port.in;

import java.util.Optional;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Public entry point used by other features (e.g. user, for login-identifier
 * resolution) to resolve a party id from an email. Cross-feature access must go
 * through a port-in use case, never through the party repository directly.
 */
public interface LoadPartyIdByEmailUseCase {

    Optional<PartyId> loadByEmail(EmailVO email);
}
