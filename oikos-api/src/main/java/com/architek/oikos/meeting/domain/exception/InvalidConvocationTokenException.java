package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.ResourceNotFoundException;

/**
 * A confirmation link that matches no convocation.
 *
 * <p>Deliberately says nothing more than that. The endpoint behind it is
 * anonymous, so its error message is readable by anyone who tries a token -
 * and "no such convocation" must not become a way to learn which tokens are
 * close to a real one.
 */
public class InvalidConvocationTokenException extends ResourceNotFoundException {

    public InvalidConvocationTokenException() {
        super("This confirmation link is not valid");
    }
}
