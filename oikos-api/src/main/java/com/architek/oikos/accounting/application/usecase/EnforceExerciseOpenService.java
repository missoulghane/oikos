package com.architek.oikos.accounting.application.usecase;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.exception.NoOpenExerciseException;
import com.architek.oikos.accounting.domain.exception.PieceDateOutsideExerciseException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Enforces "every new accounting operation is attached to the property's
 * open exercise, dated within it" (spec &sect;3/I5): every write use case
 * calls this first to resolve the exercise its new entries belong to, or
 * fail fast if it is missing/closed. A small dedicated rule-check component
 * invoked at the top of each use case.
 */
@Component
public class EnforceExerciseOpenService {

    private final AccountingExerciseRepository accountingExerciseRepository;

    public EnforceExerciseOpenService(AccountingExerciseRepository accountingExerciseRepository) {
        this.accountingExerciseRepository = accountingExerciseRepository;
    }

    public AccountingExercise requireOpenExercise(EntityId propertyId) {
        AccountingExercise exercise = accountingExerciseRepository.findOpenByPropertyId(propertyId)
                .orElseThrow(() -> new NoOpenExerciseException(propertyId));
        if (!exercise.isOpen()) {
            throw new NoOpenExerciseException(propertyId);
        }
        return exercise;
    }

    /** I5: pieceDate must fall within the given (already-open) exercise. */
    public void requirePieceDateWithin(AccountingExercise exercise, LocalDate pieceDate) {
        if (!exercise.covers(pieceDate)) {
            throw new PieceDateOutsideExerciseException(pieceDate, exercise.getId());
        }
    }
}
