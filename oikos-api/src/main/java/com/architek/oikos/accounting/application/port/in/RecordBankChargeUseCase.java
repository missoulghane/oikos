package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordBankChargeCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * P7 (spec &sect;6): records a bank fee deducted directly by the bank -
 * debit the caller-chosen class-6 charge account, credit the property's
 * BANK treasury account. Journal BQ. No dedicated entity, same as P5/P6
 * (see V14 migration comment): the journal entry is the only record.
 */
public interface RecordBankChargeUseCase {

    JournalEntryId record(RecordBankChargeCommand command);
}
