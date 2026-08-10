package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordSupplierPaymentCommand;
import com.architek.oikos.accounting.application.dto.ExpenseView;

/**
 * P4/P5 merged (spec &sect;6, Partie 2): records a direct supplier payment
 * and posts the journal entry it triggers in the same transaction - debit
 * the caller-chosen charge account, credit the caller-chosen treasury
 * account (Partie 3). No supplier party, no accrual step, no SUPPLIER
 * auxiliary ledger - a supplier is just a charge account/label. Also
 * persists an Expense so the payment shows up in the "Depenses" list.
 */
public interface RecordSupplierPaymentUseCase {

    ExpenseView record(RecordSupplierPaymentCommand command);
}
