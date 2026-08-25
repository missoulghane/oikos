package com.architek.oikos.messaging.domain.exception;

import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class RecipientGroupNotFoundException extends ResourceNotFoundException {

    public RecipientGroupNotFoundException(RecipientGroupId id) {
        super("Recipient group not found with id: " + id);
    }
}
