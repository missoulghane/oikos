package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordPayrollExpenseCommand;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * P6 (spec &sect;6): records a personnel charge accrual - debit the
 * caller-chosen class-6 charge account, credit the property's collective
 * STAFF_PAYABLE account. Journal OD (no dedicated journal for payroll in
 * the fixed 6-journal set). No dedicated entity, same as P5/P7 (see V14
 * migration comment): the journal entry is the only record.
 */
public interface RecordPayrollExpenseUseCase {

    JournalEntryId record(RecordPayrollExpenseCommand command);
}
