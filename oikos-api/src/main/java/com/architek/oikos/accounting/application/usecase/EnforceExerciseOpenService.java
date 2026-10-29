package com.architek.oikos.accounting.application.usecase;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.exception.NoExerciseCoveringDateException;
import com.architek.oikos.accounting.domain.exception.PieceDateInClosedExerciseException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Enforces "every new accounting operation is attached to the exercise its
 * piece date falls in, and that exercise is open" (spec &sect;3/I5): every
 * write use case calls this first to resolve the exercise its new entries
 * belong to, or fail fast. The exercise is found by date, not as "the" open
 * one: two exercises can be open at once (N awaiting its closing, N+1 already
 * in use), and an operation belongs to whichever of them covers its date.
 */
@Component
public class EnforceExerciseOpenService {

    private final AccountingExerciseRepository accountingExerciseRepository;

    public EnforceExerciseOpenService(AccountingExerciseRepository accountingExerciseRepository) {
        this.accountingExerciseRepository = accountingExerciseRepository;
    }

    public AccountingExercise requireOpenExerciseCovering(EntityId propertyId, LocalDate pieceDate) {
        AccountingExercise exercise = accountingExerciseRepository.findCoveringDate(propertyId, pieceDate)
                .orElseThrow(() -> new NoExerciseCoveringDateException(propertyId, pieceDate));
        if (!exercise.isOpen()) {
            throw new PieceDateInClosedExerciseException(pieceDate, exercise.getId());
        }
        return exercise;
    }
}
