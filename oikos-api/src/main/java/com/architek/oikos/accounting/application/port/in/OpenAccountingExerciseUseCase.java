package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;

public interface OpenAccountingExerciseUseCase {

    AccountingExerciseId open(OpenAccountingExerciseCommand command);
}
