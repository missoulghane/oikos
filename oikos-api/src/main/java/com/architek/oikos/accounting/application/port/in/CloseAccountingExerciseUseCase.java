package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.command.CloseAccountingExerciseCommand;
import com.architek.oikos.accounting.application.dto.ExerciseClosingView;

public interface CloseAccountingExerciseUseCase {

    ExerciseClosingView close(CloseAccountingExerciseCommand command);
}
