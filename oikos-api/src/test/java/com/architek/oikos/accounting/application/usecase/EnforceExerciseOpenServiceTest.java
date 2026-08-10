package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.exception.NoOpenExerciseException;
import com.architek.oikos.accounting.domain.exception.PeriodNotOpenException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class EnforceExerciseOpenServiceTest {

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private PeriodRepository periodRepository;

    private EnforceExerciseOpenService newService() {
        return new EnforceExerciseOpenService(accountingExerciseRepository, periodRepository);
    }

    @Test
    void returns_the_open_exercise_when_one_exists() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        when(accountingExerciseRepository.findOpenByPropertyId(propertyId)).thenReturn(Optional.of(exercise));

        AccountingExercise result = newService().requireOpenExercise(propertyId);

        assertThat(result).isEqualTo(exercise);
    }

    @Test
    void rejects_when_no_exercise_is_open_for_the_property() {
        EntityId propertyId = EntityId.newId();
        when(accountingExerciseRepository.findOpenByPropertyId(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().requireOpenExercise(propertyId))
                .isInstanceOf(NoOpenExerciseException.class);
    }

    @Test
    void I5_returns_the_open_period_covering_the_piece_date() {
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), java.time.YearMonth.of(2026, 8));
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), java.time.YearMonth.of(2026, 8)))
                .thenReturn(Optional.of(period));

        Period result = newService().requireOpenPeriod(exercise, LocalDate.of(2026, 8, 15));

        assertThat(result).isEqualTo(period);
    }

    @Test
    void I5_rejects_a_piece_date_with_no_matching_period() {
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), java.time.YearMonth.of(2026, 8)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().requireOpenPeriod(exercise, LocalDate.of(2026, 8, 15)))
                .isInstanceOf(PeriodNotOpenException.class);
    }

    @Test
    void I5_rejects_a_piece_date_whose_period_is_already_closed() {
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period closed = Period.open(PeriodId.newId(), exercise.getId(), java.time.YearMonth.of(2026, 8))
                .close(java.time.Instant.now(), EntityId.newId());
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), java.time.YearMonth.of(2026, 8)))
                .thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> newService().requireOpenPeriod(exercise, LocalDate.of(2026, 8, 15)))
                .isInstanceOf(PeriodNotOpenException.class);
    }
}
