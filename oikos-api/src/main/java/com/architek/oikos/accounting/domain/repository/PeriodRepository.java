package com.architek.oikos.accounting.domain.repository;

import java.time.YearMonth;
import java.util.Optional;

import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;

public interface PeriodRepository {

    Period save(Period period);

    Optional<Period> findById(PeriodId id);

    Optional<Period> findByExerciseIdAndYearMonth(AccountingExerciseId exerciseId, YearMonth yearMonth);
}
