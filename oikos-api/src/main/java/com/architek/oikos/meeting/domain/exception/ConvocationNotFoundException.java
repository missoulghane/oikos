package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class ConvocationNotFoundException extends ResourceNotFoundException {

    public ConvocationNotFoundException(ConvocationId id) {
        super("Convocation not found with id: " + id);
    }
}
