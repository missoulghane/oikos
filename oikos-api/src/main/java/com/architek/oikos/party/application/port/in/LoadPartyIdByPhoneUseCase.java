package com.architek.oikos.party.application.port.in;

import java.util.Optional;

import com.architek.oikos.party.domain.valueobject.PartyId;

/**
 * Public entry point used by other features (e.g. user, for login-identifier
 * resolution) to resolve a party id from a phone number. Cross-feature access
 * must go through a port-in use case, never through the party repository directly.
 */
public interface LoadPartyIdByPhoneUseCase {

    Optional<PartyId> loadByPhone(String phone);
}
