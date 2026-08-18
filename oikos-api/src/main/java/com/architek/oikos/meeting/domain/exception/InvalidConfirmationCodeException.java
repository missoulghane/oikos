package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * The code of the lot, typed on the confirmation page, does not match the
 * convocation the link points at.
 *
 * <p>Distinct from {@link InvalidConvocationTokenException} on purpose, and it
 * is not an oracle: the visitor holding the link already sees which lot it
 * concerns, so being told "that is not this lot's code" reveals nothing they
 * did not have. Saying "this link is not valid" instead would send someone who
 * mistyped six characters looking for a new convocation.
 *
 * <p>What keeps it from becoming a way to guess a code is the same cap as the
 * paper path - ConfirmationAttemptLimiterPort, ten failures per quarter hour
 * (ADR 0002 §13).
 */
public class InvalidConfirmationCodeException extends BusinessException {

    public InvalidConfirmationCodeException() {
        super("This code does not match the lot of this convocation");
    }
}
