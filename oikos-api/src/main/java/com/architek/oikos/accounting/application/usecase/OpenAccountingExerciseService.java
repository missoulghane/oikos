package com.architek.oikos.accounting.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.ExerciseNotContiguousException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.exception.TooManyOpenExercisesException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;

/**
 * Spec &sect;3: a property's exercises follow one another - each starts the day
 * after the previous one ends, so they never overlap and no day is left
 * outside an exercise. Up to two may be open at once: N, kept open until its
 * accounts are approved, and N+1, already receiving the new year's fund calls.
 */
@Component
public class OpenAccountingExerciseService implements OpenAccountingExerciseUseCase {

    static final int MAX_OPEN_EXERCISES = 2;

    private final AccountingExerciseRepository accountingExerciseRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public OpenAccountingExerciseService(AccountingExerciseRepository accountingExerciseRepository,
                                          PropertyDirectoryPort propertyDirectoryPort) {
        this.accountingExerciseRepository = accountingExerciseRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
    }

    @Override
    @Transactional
    public AccountingExerciseId open(OpenAccountingExerciseCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }
        List<AccountingExercise> existing = accountingExerciseRepository.findAllByPropertyId(command.propertyId());
        if (existing.stream().filter(AccountingExercise::isOpen).count() >= MAX_OPEN_EXERCISES) {
            throw new TooManyOpenExercisesException(command.propertyId(), MAX_OPEN_EXERCISES);
        }
        if (!existing.isEmpty()) {
            LocalDate expectedStartDate = existing.getLast().getEndDate().plusDays(1);
            if (!expectedStartDate.equals(command.startDate())) {
                throw new ExerciseNotContiguousException(command.propertyId(), expectedStartDate,
                        command.startDate());
            }
        }
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), command.propertyId(),
                command.label(), command.startDate(), command.endDate(), command.comment());
        return accountingExerciseRepository.save(exercise).getId();
    }
}
