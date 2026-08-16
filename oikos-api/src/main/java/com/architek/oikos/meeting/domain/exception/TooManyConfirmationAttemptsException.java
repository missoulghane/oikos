package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * Too many wrong codes, too fast, from one place.
 *
 * <p>The six-character code is about 34^6 - some six orders of magnitude below
 * the convocation's token. A secret that small is only defensible if it cannot
 * be tried in a loop, so this is not an ergonomic nicety: it is the other half
 * of the decision to have a short code at all (ADR 0002 §13).
 */
public class TooManyConfirmationAttemptsException extends BusinessException {

    public TooManyConfirmationAttemptsException() {
        super("Too many attempts with an invalid code - try again later, or use the link from your convocation");
    }
}
