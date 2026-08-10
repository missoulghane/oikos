package com.architek.oikos.accounting.domain.exception;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class JournalEntryNotFoundException extends ResourceNotFoundException {

    public JournalEntryNotFoundException(JournalEntryId id) {
        super("Journal entry not found with id: " + id);
    }
}
