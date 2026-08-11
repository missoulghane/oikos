package com.architek.oikos.accounting.application.usecase;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.exception.NoOpenExerciseException;
import com.architek.oikos.accounting.domain.exception.PeriodNotOpenException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Enforces "every new accounting operation is attached to the property's
 * open exercise, in an open period" (spec &sect;3/I5): every write use case
 * calls this first to resolve the exercise/period its new entries belong
 * to, or fail fast if either is missing/closed. A small dedicated
 * rule-check component invoked at the top of each use case.
 */
@Component
public class EnforceExerciseOpenService {

    private final AccountingExerciseRepository accountingExerciseRepository;
    private final PeriodRepository periodRepository;

    public EnforceExerciseOpenService(AccountingExerciseRepository accountingExerciseRepository,
                                       PeriodRepository periodRepository) {
        this.accountingExerciseRepository = accountingExerciseRepository;
        this.periodRepository = periodRepository;
    }

    public AccountingExercise requireOpenExercise(EntityId propertyId) {
        AccountingExercise exercise = accountingExerciseRepository.findOpenByPropertyId(propertyId)
                .orElseThrow(() -> new NoOpenExerciseException(propertyId));
        if (!exercise.isOpen()) {
            throw new NoOpenExerciseException(propertyId);
        }
        return exercise;
    }

    /** I5: pieceDate must fall in an OPEN period of the given (already-open) exercise. */
    public Period requireOpenPeriod(AccountingExercise exercise, LocalDate pieceDate) {
        Period period = periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.from(pieceDate))
                .orElseThrow(() -> new PeriodNotOpenException(pieceDate));
        if (!period.isOpen()) {
            throw new PeriodNotOpenException(pieceDate);
        }
        return period;
    }
}
