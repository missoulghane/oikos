package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

public interface CreateJournalEntryDraftUseCase {

    JournalEntryId create(CreateJournalEntryDraftCommand command);
}
