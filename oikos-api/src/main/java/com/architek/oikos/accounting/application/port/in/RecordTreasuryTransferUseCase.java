package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordTreasuryTransferCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

public interface RecordTreasuryTransferUseCase {

    JournalEntryId record(RecordTreasuryTransferCommand command);
}
