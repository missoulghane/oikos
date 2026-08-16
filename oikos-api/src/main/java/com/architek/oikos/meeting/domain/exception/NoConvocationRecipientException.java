package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * The lot has nobody to write to - no owner recorded, or none of its owners
 * has an email address. Not a silent no-op: an unreachable lot is precisely
 * what the syndic has to know about, so it surfaces as an error on the
 * individual send and as a FAILED row in the tracking table for a bulk run.
 */
public class NoConvocationRecipientException extends BusinessException {

    public NoConvocationRecipientException(String unitNumber) {
        super("Lot " + unitNumber + " has no owner with an email address - convoke it by post and record its "
                + "delivery status manually");
    }
}
