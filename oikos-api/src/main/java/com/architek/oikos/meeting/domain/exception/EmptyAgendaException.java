package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * A meeting with nothing to decide cannot be scheduled. The agenda stays
 * editable afterwards (ADR 0002 §8), so this is a "not ready yet" guard, not a
 * point of no return.
 */
public class EmptyAgendaException extends BusinessException {

    public EmptyAgendaException() {
        super("A general meeting must have at least one agenda item before it can be scheduled");
    }
}
