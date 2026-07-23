package com.architek.oikos.party.domain.exception;

import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class PartyNotFoundException extends ResourceNotFoundException {

    public PartyNotFoundException(PartyId id) {
        super("Party not found with id: " + id);
    }
}
