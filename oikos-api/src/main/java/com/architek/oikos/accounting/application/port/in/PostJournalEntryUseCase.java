package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.dto.JournalEntryView;

public interface PostJournalEntryUseCase {

    JournalEntryView post(PostJournalEntryCommand command);
}
