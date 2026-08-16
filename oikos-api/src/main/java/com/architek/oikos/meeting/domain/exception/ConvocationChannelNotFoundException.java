package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * A channel code that is in no catalog row. Since the channels became data, an
 * unknown code is a 404 on a reference the caller invented, not a validation
 * error on a fixed enum.
 */
public class ConvocationChannelNotFoundException extends ResourceNotFoundException {

    public ConvocationChannelNotFoundException(ChannelCode code) {
        super("Convocation channel not found with code: " + code);
    }
}
