package com.architek.oikos.installment.domain.exception;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.shared.exception.CodedException;
import com.architek.oikos.shared.exception.ErrorCodes;

/** A unit has no advance to impute, or no unpaid fund call to impute it on. */
public class NothingToRegularizeException extends BusinessException implements CodedException {

    public NothingToRegularizeException(EntityId unitId) {
        super("Nothing to regularize for unit " + unitId
                + ": no available advance, or no unsettled fund call to impute it on");
    }

    @Override
    public String errorCode() {
        return ErrorCodes.NOTHING_TO_REGULARIZE;
    }
}
