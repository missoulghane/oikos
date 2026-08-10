package com.architek.oikos.accounting.web.response;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * Minimal response for write endpoints that post a journal entry but have
 * no other persisted entity of their own (P5/P6/P7 - see V14 migration
 * comment): the journal entry, already inspectable via
 * GET /accounting/entries/{id}, is the only record.
 */
public record JournalEntryReferenceResponse(String journalEntryId) {

    public static JournalEntryReferenceResponse from(JournalEntryId id) {
        return new JournalEntryReferenceResponse(id.toString());
    }
}
