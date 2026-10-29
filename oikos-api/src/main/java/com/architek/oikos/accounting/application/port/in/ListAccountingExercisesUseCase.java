package com.architek.oikos.accounting.application.port.in;

import java.util.List;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.query.ListAccountingExercisesQuery;

public interface ListAccountingExercisesUseCase {

    List<AccountingExerciseView> list(ListAccountingExercisesQuery query);
}
