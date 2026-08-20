package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.ReopenPeriodCommand;
import com.architek.oikos.accounting.application.dto.PeriodView;
import com.architek.oikos.accounting.domain.exception.PeriodNotClosedException;
import com.architek.oikos.accounting.domain.exception.PeriodNotFoundException;
import com.architek.oikos.accounting.domain.exception.PeriodNotReopenableException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ReopenPeriodServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId USER_ID = EntityId.newId();
    private static final AccountingExerciseId EXERCISE_ID = AccountingExerciseId.newId();

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private PeriodRepository periodRepository;

    private ReopenPeriodService newService() {
        return new ReopenPeriodService(enforceExerciseOpenService, periodRepository);
    }

    private static AccountingExercise openExercise() {
        return AccountingExercise.open(EXERCISE_ID, PROPERTY_ID, "2026", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null);
    }

    private static Period closedPeriod(YearMonth month) {
        return Period.reconstruct(PeriodId.newId(), EXERCISE_ID, month, PeriodStatus.CLOSED,
                Instant.parse("2026-04-01T10:00:00Z"), EntityId.newId(), null, null);
    }

    private static Period openPeriod(YearMonth month) {
        return Period.open(PeriodId.newId(), EXERCISE_ID, month);
    }

    @Test
    void reopens_the_last_closed_period() {
        YearMonth march = YearMonth.of(2026, 3);
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, march))
                .thenReturn(Optional.of(closedPeriod(march)));
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(
                closedPeriod(YearMonth.of(2026, 1)), closedPeriod(YearMonth.of(2026, 2)), closedPeriod(march),
                openPeriod(YearMonth.of(2026, 4))));
        when(periodRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PeriodView view = newService().reopen(new ReopenPeriodCommand(PROPERTY_ID, march, USER_ID));

        assertThat(view.status()).isEqualTo(PeriodStatus.OPEN);
        assertThat(view.closedAt()).isNull();
    }

    @Test
    void keeps_a_trace_of_who_reopened_it() {
        // Le prix minimal d'un geste que la comptabilité interdit d'ordinaire :
        // la période redevient ouverte, mais pas comme si de rien n'était.
        YearMonth march = YearMonth.of(2026, 3);
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, march))
                .thenReturn(Optional.of(closedPeriod(march)));
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(closedPeriod(march)));
        when(periodRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().reopen(new ReopenPeriodCommand(PROPERTY_ID, march, USER_ID));

        ArgumentCaptor<Period> captor = ArgumentCaptor.forClass(Period.class);
        verify(periodRepository).save(captor.capture());
        assertThat(captor.getValue().getReopenedByUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getReopenedAt()).isNotNull();
    }

    @Test
    void refuses_to_pierce_the_run_of_closed_periods() {
        // Rouvrir février entre janvier et mars clos produirait un état que la
        // clôture elle-même n'aurait jamais pu créer.
        YearMonth february = YearMonth.of(2026, 2);
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, february))
                .thenReturn(Optional.of(closedPeriod(february)));
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(
                closedPeriod(YearMonth.of(2026, 1)), closedPeriod(february), closedPeriod(YearMonth.of(2026, 3))));

        assertThatThrownBy(() -> newService().reopen(new ReopenPeriodCommand(PROPERTY_ID, february, USER_ID)))
                .isInstanceOf(PeriodNotReopenableException.class)
                .hasMessageContaining("2026-03");
        verify(periodRepository, never()).save(any());
    }

    @Test
    void refuses_a_period_that_is_already_open() {
        YearMonth april = YearMonth.of(2026, 4);
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, april))
                .thenReturn(Optional.of(openPeriod(april)));
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(openPeriod(april)));

        assertThatThrownBy(() -> newService().reopen(new ReopenPeriodCommand(PROPERTY_ID, april, USER_ID)))
                .isInstanceOf(PeriodNotClosedException.class);
        verify(periodRepository, never()).save(any());
    }

    @Test
    void refuses_a_month_outside_the_open_exercise() {
        YearMonth outside = YearMonth.of(2025, 12);
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, outside)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().reopen(new ReopenPeriodCommand(PROPERTY_ID, outside, USER_ID)))
                .isInstanceOf(PeriodNotFoundException.class);
    }
}
