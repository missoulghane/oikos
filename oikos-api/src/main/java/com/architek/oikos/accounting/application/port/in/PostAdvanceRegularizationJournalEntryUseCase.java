package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.PostAdvanceRegularizationJournalEntryCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

public interface PostAdvanceRegularizationJournalEntryUseCase {

    JournalEntryId post(PostAdvanceRegularizationJournalEntryCommand command);
}
