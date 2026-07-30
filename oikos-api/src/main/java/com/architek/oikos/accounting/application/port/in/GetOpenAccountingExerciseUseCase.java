package com.architek.oikos.accounting.application.port.in;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.query.GetOpenAccountingExerciseQuery;

public interface GetOpenAccountingExerciseUseCase {

    AccountingExerciseView get(GetOpenAccountingExerciseQuery query);
}
