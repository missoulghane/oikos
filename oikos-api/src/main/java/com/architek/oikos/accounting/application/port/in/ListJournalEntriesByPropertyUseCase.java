package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.query.ListJournalEntriesByPropertyQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListJournalEntriesByPropertyUseCase {

    Page<JournalEntryView> list(ListJournalEntriesByPropertyQuery query);
}
