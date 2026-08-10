package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallStatus;
import com.architek.oikos.shared.exception.ConflictException;

/** An InstallmentCall lifecycle transition (issue/post/cancel) was attempted from an incompatible status. */
public class InvalidInstallmentCallTransitionException extends ConflictException {

    public InvalidInstallmentCallTransitionException(InstallmentCallId id, InstallmentCallStatus currentStatus,
                                                       String attemptedTransition) {
        super("Installment call " + id + " is " + currentStatus + " and cannot " + attemptedTransition);
    }
}
