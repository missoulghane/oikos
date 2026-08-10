package com.architek.oikos.accounting.application.usecase;

import java.time.YearMonth;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.ExerciseAlreadyOpenException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;

/**
 * Spec &sect;3: a property has only one open exercise at a time. Also
 * generates one OPEN Period per month spanned by the exercise (spec
 * &sect;4.1: "periodes mensuelles avec statut propre") - there is no
 * separate "open a period" endpoint in the API surface, so periods are
 * eagerly created here rather than lazily on first use.
 */
@Component
public class OpenAccountingExerciseService implements OpenAccountingExerciseUseCase {

    private final AccountingExerciseRepository accountingExerciseRepository;
    private final PeriodRepository periodRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public OpenAccountingExerciseService(AccountingExerciseRepository accountingExerciseRepository,
                                          PeriodRepository periodRepository,
                                          PropertyDirectoryPort propertyDirectoryPort) {
        this.accountingExerciseRepository = accountingExerciseRepository;
        this.periodRepository = periodRepository;
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
        AccountingExerciseId exerciseId = accountingExerciseRepository.save(exercise).getId();

        YearMonth month = YearMonth.from(command.startDate());
        YearMonth lastMonth = YearMonth.from(command.endDate());
        while (!month.isAfter(lastMonth)) {
            periodRepository.save(Period.open(PeriodId.newId(), exerciseId, month));
            month = month.plusMonths(1);
        }

        return exerciseId;
    }
}
