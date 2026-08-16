package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.MinutesStatus;
import com.architek.oikos.shared.exception.ConflictException;

/**
 * Editing minutes that have been validated. Validation is what freezes the
 * text - if it could still be changed afterwards, validating would mean
 * nothing and the published PDF would not be the document that was approved.
 */
public class MinutesLockedException extends ConflictException {

    public MinutesLockedException(MinutesStatus status) {
        super("Minutes can only be edited while they are DRAFT, current status: " + status);
    }
}
