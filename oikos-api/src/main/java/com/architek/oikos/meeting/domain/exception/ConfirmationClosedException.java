package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * The confirmation link, once the session has opened.
 *
 * <p>Announcing that one will attend is a statement about a meeting that has
 * not started; from the moment it has, presence is the check-in and nothing
 * else (ADR 0002 §5). Accepting a confirmation past that point would let a
 * copropriétaire appear to have "confirmed" a meeting they never came to, on
 * a record the minutes are drawn from.
 */
public class ConfirmationClosedException extends BusinessException {

    public ConfirmationClosedException() {
        super("This general meeting has already started - attendance is now recorded by signing in, "
                + "not by confirming");
    }
}
