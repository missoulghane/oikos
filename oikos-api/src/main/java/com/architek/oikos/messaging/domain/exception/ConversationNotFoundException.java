package com.architek.oikos.messaging.domain.exception;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class ConversationNotFoundException extends ResourceNotFoundException {

    public ConversationNotFoundException(ConversationId id) {
        super("Conversation not found with id: " + id);
    }
}
