package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByTreasuryAccountQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListJournalEntriesByTreasuryAccountUseCase {

    Page<JournalEntryView> list(ListJournalEntriesByTreasuryAccountQuery query);
}
