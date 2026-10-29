package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.ExerciseNotContiguousException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.exception.TooManyOpenExercisesException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class OpenAccountingExerciseServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    private OpenAccountingExerciseService newService() {
        return new OpenAccountingExerciseService(accountingExerciseRepository, propertyDirectoryPort);
    }

    @Test
    void opening_an_exercise_for_a_missing_property_is_rejected() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(false);

        assertThatThrownBy(() -> newService().open(command(2026))).isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void the_first_exercise_of_a_property_may_start_on_any_date() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of());
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AccountingExerciseId id = newService().open(command(2026));

        assertThat(id).isNotNull();
    }

    @Test
    void the_next_exercise_can_be_opened_while_the_previous_one_is_still_open() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(exercise(2026)));
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AccountingExerciseId id = newService().open(command(2027));

        assertThat(id).isNotNull();
    }

    @Test
    void a_third_open_exercise_is_rejected() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID))
                .thenReturn(List.of(exercise(2026), exercise(2027)));

        assertThatThrownBy(() -> newService().open(command(2028)))
                .isInstanceOf(TooManyOpenExercisesException.class);
    }

    @Test
    void a_closed_exercise_does_not_count_towards_the_open_limit() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        AccountingExercise closed2026 = exercise(2026).close(Instant.parse("2027-04-01T10:00:00Z"), EntityId.newId());
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID))
                .thenReturn(List.of(closed2026, exercise(2027)));
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(newService().open(command(2028))).isNotNull();
    }

    @Test
    void an_exercise_leaving_a_gap_after_the_last_one_is_rejected() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(exercise(2026)));

        assertThatThrownBy(() -> newService().open(command(2028)))
                .isInstanceOf(ExerciseNotContiguousException.class);
    }

    @Test
    void an_exercise_overlapping_the_last_one_is_rejected() {
        when(propertyDirectoryPort.exists(PROPERTY_ID)).thenReturn(true);
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(exercise(2026)));
        OpenAccountingExerciseCommand overlapping = new OpenAccountingExerciseCommand(PROPERTY_ID, "Exercice 2027",
                LocalDate.of(2026, 7, 1), LocalDate.of(2027, 6, 30), null);

        assertThatThrownBy(() -> newService().open(overlapping)).isInstanceOf(ExerciseNotContiguousException.class);
    }

    private static OpenAccountingExerciseCommand command(int year) {
        return new OpenAccountingExerciseCommand(PROPERTY_ID, "Exercice " + year, LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31), null);
    }

    private static AccountingExercise exercise(int year) {
        return AccountingExercise.open(AccountingExerciseId.newId(), PROPERTY_ID, "Exercice " + year,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), null);
    }
}
