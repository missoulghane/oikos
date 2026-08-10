package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.shared.exception.ConflictException;

/** I4: a journal entry is immutable once it has left DRAFT (spec &sect;12: ECRITURE_IMMUABLE). */
public class JournalEntryNotDraftException extends ConflictException {

    public JournalEntryNotDraftException(JournalEntryId id, JournalEntryStatus status) {
        super("Journal entry " + id + " is not DRAFT (status: " + status + ") and cannot be mutated");
    }
}
