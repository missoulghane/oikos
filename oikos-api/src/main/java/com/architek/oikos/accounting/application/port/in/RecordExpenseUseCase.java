package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.RecordExpenseCommand;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;

public interface RecordExpenseUseCase {

    ExpenseId record(RecordExpenseCommand command);
}
