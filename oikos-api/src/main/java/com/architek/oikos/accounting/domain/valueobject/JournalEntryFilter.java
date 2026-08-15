package com.architek.oikos.accounting.domain.valueobject;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.pagination.SortDirection;

/**
 * Search criteria for a treasury account's operations listing (accounting
 * overview "click an account, see its operations" flow). search matches
 * externalReference (case-insensitive, contains); null/blank fields mean "no
 * filter" so callers never need a separate unfiltered query, mirroring
 * InstallmentFilter's convention.
 *
 * sortField null keeps the listing's own default order, most recent piece date
 * first.
 */
public record JournalEntryFilter(LocalDate pieceDateFrom, LocalDate pieceDateTo, String search,
                                  JournalEntryStatus status, JournalEntrySortField sortField,
                                  SortDirection sortDirection) {

    public JournalEntryFilter(LocalDate pieceDateFrom, LocalDate pieceDateTo, String search,
                               JournalEntryStatus status) {
        this(pieceDateFrom, pieceDateTo, search, status, null, null);
    }

    public JournalEntryFilter {
        if (pieceDateFrom != null && pieceDateTo != null && pieceDateFrom.isAfter(pieceDateTo)) {
            throw new IllegalArgumentException("pieceDateFrom must not be after pieceDateTo");
        }
        search = (search == null || search.isBlank()) ? null : search.trim();
    }

    public static JournalEntryFilter defaultFilter() {
        return new JournalEntryFilter(null, null, null, null, null, null);
    }
}
