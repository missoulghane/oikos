package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.ExerciseAlreadyOpenException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class OpenAccountingExerciseServiceTest {

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private PeriodRepository periodRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private OpenAccountingExerciseService newService() {
        return new OpenAccountingExerciseService(accountingExerciseRepository, periodRepository, propertyDirectoryPort);
    }

    @Test
    void opening_an_exercise_for_a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        OpenAccountingExerciseCommand command = new OpenAccountingExerciseCommand(propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        assertThatThrownBy(() -> newService().open(command)).isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void opening_a_second_exercise_while_one_is_already_open_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(accountingExerciseRepository.existsOpenByPropertyId(propertyId)).thenReturn(true);

        OpenAccountingExerciseCommand command = new OpenAccountingExerciseCommand(propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        assertThatThrownBy(() -> newService().open(command)).isInstanceOf(ExerciseAlreadyOpenException.class);
    }

    @Test
    void opening_an_exercise_persists_it_when_none_is_open() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(accountingExerciseRepository.existsOpenByPropertyId(propertyId)).thenReturn(false);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OpenAccountingExerciseCommand command = new OpenAccountingExerciseCommand(propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "Premier exercice");

        AccountingExerciseId id = newService().open(command);

        org.assertj.core.api.Assertions.assertThat(id).isNotNull();
    }

    @Test
    void opening_an_exercise_generates_one_open_period_per_month_spanned() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(accountingExerciseRepository.existsOpenByPropertyId(propertyId)).thenReturn(false);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OpenAccountingExerciseCommand command = new OpenAccountingExerciseCommand(propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), null);

        newService().open(command);

        org.mockito.ArgumentCaptor<com.architek.oikos.accounting.domain.model.Period> captor =
                org.mockito.ArgumentCaptor.forClass(com.architek.oikos.accounting.domain.model.Period.class);
        org.mockito.Mockito.verify(periodRepository, org.mockito.Mockito.times(3)).save(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getAllValues())
                .extracting(com.architek.oikos.accounting.domain.model.Period::getYearMonth)
                .containsExactly(java.time.YearMonth.of(2026, 1), java.time.YearMonth.of(2026, 2),
                        java.time.YearMonth.of(2026, 3));
    }
}
