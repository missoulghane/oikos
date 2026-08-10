package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.PostOwnerPaymentJournalEntryCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * P2/P3 (spec &sect;4.1/&sect;4.2): posts the treasury journal entry (BQ or
 * CA, depending on the payment mode) for an owner payment - debit the
 * treasury account for the full amount received, credit the unit's own
 * UNIT_RECEIVABLE account for the portion imputed to its unsettled fund
 * calls (FIFO, computed by the caller), credit the property's collective
 * UNIT_ADVANCE account for any leftover. Public entry point for
 * installment's RecordOwnerPaymentUseCase (cross-module, via installment's
 * own out-port - never called with accounting's internals, per the
 * cross-module convention).
 */
public interface PostOwnerPaymentJournalEntryUseCase {

    JournalEntryId post(PostOwnerPaymentJournalEntryCommand command);
}
