package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class AccountingExerciseTest {

    @Test
    void open_creates_an_open_exercise_with_no_closing_info() {
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        assertThat(exercise.getStatus()).isEqualTo(ExerciseStatus.OPEN);
        assertThat(exercise.isOpen()).isTrue();
        assertThat(exercise.getClosedAt()).isNull();
        assertThat(exercise.getClosedByUserId()).isNull();
    }

    @Test
    void end_date_must_be_after_start_date() {
        assertThatThrownBy(() -> AccountingExercise.open(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2026", LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_reconstructed_closed_exercise_is_not_open() {
        AccountingExercise exercise = AccountingExercise.reconstruct(AccountingExerciseId.newId(), EntityId.newId(),
                "Exercice 2025", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), ExerciseStatus.CLOSED,
                java.time.Instant.now(), EntityId.newId(), "Cloture annuelle");

        assertThat(exercise.isOpen()).isFalse();
    }
}
