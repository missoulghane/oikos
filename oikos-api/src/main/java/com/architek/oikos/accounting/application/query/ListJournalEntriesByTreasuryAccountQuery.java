package com.architek.oikos.accounting.application.query;

import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListJournalEntriesByTreasuryAccountQuery(EntityId propertyId, LedgerAccountId treasuryAccountId,
                                                         JournalEntryFilter filter, PageRequest pageRequest) {
}
