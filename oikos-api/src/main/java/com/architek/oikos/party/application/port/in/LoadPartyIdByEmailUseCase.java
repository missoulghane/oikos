package com.architek.oikos.party.application.port.in;

import java.util.Optional;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Public entry point used by other features (e.g. property, for its
 * get-or-create-owner-by-email flow) to resolve a party id from an email,
 * scoped to one property - the same email may resolve to a different party
 * (or none) in a different property. Cross-feature access must go through a
 * port-in use case, never through the party repository directly.
 */
public interface LoadPartyIdByEmailUseCase {

    Optional<PartyId> loadByEmail(EntityId propertyId, EmailVO email);
}
