package com.architek.oikos.accounting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface JournalEntryRepository {

    JournalEntry save(JournalEntry entry);

    Optional<JournalEntry> findById(JournalEntryId id);

    /** I7: the next piece number to allocate for (property, exercise, journal), under a row lock. */
    int nextPieceNumber(EntityId propertyId, AccountingExerciseId exerciseId, JournalCode journalCode);

    /** Most recent piece date first. */
    Page<JournalEntry> findPageByPropertyId(EntityId propertyId, PageRequest pageRequest);

    /** Every entry attached to a period, regardless of status - used by P8's closing checks. */
    List<JournalEntry> findAllByPeriodId(PeriodId periodId);
}
