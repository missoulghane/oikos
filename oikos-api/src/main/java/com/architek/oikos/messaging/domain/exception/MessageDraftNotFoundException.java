package com.architek.oikos.messaging.domain.exception;

import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class MessageDraftNotFoundException extends ResourceNotFoundException {

    public MessageDraftNotFoundException(MessageDraftId id) {
        super("Message draft not found with id: " + id);
    }
}
