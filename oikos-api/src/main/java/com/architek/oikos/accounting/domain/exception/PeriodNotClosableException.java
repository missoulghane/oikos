package com.architek.oikos.accounting.domain.exception;

import java.util.List;

import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.exception.ConflictException;

/** P8 (spec &sect;12: EXERCICE_NON_CLOTURABLE): one or more blocking checks failed (PeriodClosingValidator). */
public class PeriodNotClosableException extends ConflictException {

    public PeriodNotClosableException(PeriodId id, List<String> violations) {
        super("Period " + id + " cannot be closed: " + String.join("; ", violations));
    }
}
