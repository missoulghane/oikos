package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.ExerciseAlreadyOpenException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;

/** Spec &sect;3: a property has only one open exercise at a time. */
@Component
public class OpenAccountingExerciseService implements OpenAccountingExerciseUseCase {

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
        if (accountingExerciseRepository.existsOpenByPropertyId(command.propertyId())) {
            throw new ExerciseAlreadyOpenException(command.propertyId());
        }
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), command.propertyId(),
                command.label(), command.startDate(), command.endDate(), command.comment());
        return accountingExerciseRepository.save(exercise).getId();
    }
}
