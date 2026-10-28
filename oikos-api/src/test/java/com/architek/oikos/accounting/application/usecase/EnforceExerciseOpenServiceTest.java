package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.exception.NoOpenExerciseException;
import com.architek.oikos.accounting.domain.exception.PieceDateOutsideExerciseException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class EnforceExerciseOpenServiceTest {

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    private EnforceExerciseOpenService newService() {
        return new EnforceExerciseOpenService(accountingExerciseRepository);
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
    void I5_accepts_a_piece_date_within_the_exercise_bounds_included() {
        AccountingExercise exercise = exercise2026();

        assertThatCode(() -> newService().requirePieceDateWithin(exercise, LocalDate.of(2026, 1, 1)))
                .doesNotThrowAnyException();
        assertThatCode(() -> newService().requirePieceDateWithin(exercise, LocalDate.of(2026, 8, 15)))
                .doesNotThrowAnyException();
        assertThatCode(() -> newService().requirePieceDateWithin(exercise, LocalDate.of(2026, 12, 31)))
                .doesNotThrowAnyException();
    }

    @Test
    void I5_rejects_a_piece_date_before_the_exercise_starts() {
        assertThatThrownBy(() -> newService().requirePieceDateWithin(exercise2026(), LocalDate.of(2025, 12, 31)))
                .isInstanceOf(PieceDateOutsideExerciseException.class);
    }

    @Test
    void I5_rejects_a_piece_date_after_the_exercise_ends() {
        assertThatThrownBy(() -> newService().requirePieceDateWithin(exercise2026(), LocalDate.of(2027, 1, 1)))
                .isInstanceOf(PieceDateOutsideExerciseException.class);
    }

    private static AccountingExercise exercise2026() {
        return AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(), "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
    }
}
