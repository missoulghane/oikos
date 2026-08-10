package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.shared.exception.BusinessException;

/** Spec &sect;3.3: AN is reserved to the engine (closing/a-nouveaux), never user-saisissable. */
public class NonPostableJournalException extends BusinessException {

    public NonPostableJournalException(JournalCode journalCode) {
        super("Journal " + journalCode + " is reserved to the engine and cannot be posted to directly");
    }
}
