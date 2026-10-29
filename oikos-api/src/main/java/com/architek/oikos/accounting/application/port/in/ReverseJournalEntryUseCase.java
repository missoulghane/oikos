package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.ReverseJournalEntryCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * P10 (contre-passation): cancels a POSTED entry by posting its mirror and
 * marking it REVERSED. Returns the mirror entry's id. Public entry point for
 * other modules (installment's fund-call cancellation), via their own out-port.
 */
public interface ReverseJournalEntryUseCase {

    JournalEntryId reverse(ReverseJournalEntryCommand command);
}
