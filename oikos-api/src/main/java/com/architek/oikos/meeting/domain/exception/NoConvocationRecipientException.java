package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * The lot has nobody to write to - no owner recorded, or none of its owners
 * has an email address. Not a silent no-op: an unreachable lot is precisely
 * what the syndic has to know about, so it surfaces as an error on the
 * individual send and as a FAILED row in the tracking table for a bulk run.
 */
public class NoConvocationRecipientException extends BusinessException {

    private NoConvocationRecipientException(String message) {
        super(message);
    }

    public static NoConvocationRecipientException noEmail(String unitNumber) {
        return new NoConvocationRecipientException("Lot " + unitNumber + " has no owner with an email address - "
                + "convoke it by post and record its delivery status manually");
    }

    /**
     * Distinct from {@link #noEmail}, and worth its own wording: a lot whose
     * owners have an email but no account is perfectly reachable - just not by
     * this channel. Telling the syndic to post a letter would be wrong advice.
     */
    public static NoConvocationRecipientException noAccount(String unitNumber) {
        return new NoConvocationRecipientException("Lot " + unitNumber + " has no owner with an application account - "
                + "convoke it by email or by post instead");
    }
}
