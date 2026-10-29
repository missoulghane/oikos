package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ConflictException;
import com.architek.oikos.shared.exception.ErrorCodes;

/**
 * At least one installment of the call has already been settled, in part or
 * in full (payment or advance imputed): cancelling the call would first
 * require undoing those imputations, which is not supported.
 */
public class InstallmentCallSettledException extends ConflictException implements CodedException {

    public InstallmentCallSettledException(InstallmentCallId id) {
        super("Installment call " + id + " has settled installments and cannot be cancelled");
    }

    @Override
    public String errorCode() {
        return ErrorCodes.INSTALLMENT_CALL_SETTLED;
    }
}
