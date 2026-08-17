package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * The means of reception named on a reply is in no catalog row. Surfaced rather
 * than left to the foreign key: the column references reply_medium, so an
 * unknown code fails at commit either way, and a constraint violation tells the
 * caller nothing it can act on.
 */
public class ReplyMediumNotFoundException extends ResourceNotFoundException {

    public ReplyMediumNotFoundException(ReplyMediumCode code) {
        super("Reply medium not found with code: " + code);
    }
}
