package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.query.GetJournalEntryQuery;

public interface GetJournalEntryUseCase {

    JournalEntryView get(GetJournalEntryQuery query);
}
