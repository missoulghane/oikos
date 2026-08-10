package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.shared.exception.ConflictException;

/** P10: only a POSTED entry can be reversed (contre-passation). */
public class JournalEntryNotPostedException extends ConflictException {

    public JournalEntryNotPostedException(JournalEntryId id, JournalEntryStatus status) {
        super("Journal entry " + id + " is not POSTED (status: " + status + ") and cannot be reversed");
    }
}
