package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.FinancialJournalEntryView;
import com.architek.oikos.accounting.application.query.ListFinancialJournalEntriesQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListFinancialJournalEntriesUseCase {

    Page<FinancialJournalEntryView> list(ListFinancialJournalEntriesQuery query);
}
