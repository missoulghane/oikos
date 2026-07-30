package com.architek.oikos.accounting.application.query;

import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryFilter;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListFinancialJournalEntriesQuery(EntityId propertyId, FinancialJournalEntryFilter filter,
                                                PageRequest pageRequest) {
}
