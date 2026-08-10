package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.exception.ConflictException;

/** A period can only be closed once (spec &sect;4.1). */
public class PeriodAlreadyClosedException extends ConflictException {

    public PeriodAlreadyClosedException(PeriodId id) {
        super("Period " + id + " is already closed");
    }
}
