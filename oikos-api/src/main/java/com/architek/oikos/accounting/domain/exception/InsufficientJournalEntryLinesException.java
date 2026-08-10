package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/** I2: a journal entry needs at least 2 lines (spec &sect;5). */
public class InsufficientJournalEntryLinesException extends BusinessException {

    public InsufficientJournalEntryLinesException(int lineCount) {
        super("A journal entry requires at least 2 lines, got: " + lineCount);
    }
}
