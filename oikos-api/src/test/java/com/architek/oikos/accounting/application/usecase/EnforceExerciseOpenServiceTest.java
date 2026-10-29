package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.exception.NoExerciseCoveringDateException;
import com.architek.oikos.accounting.domain.exception.PieceDateInClosedExerciseException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class EnforceExerciseOpenServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    private EnforceExerciseOpenService newService() {
        return new EnforceExerciseOpenService(accountingExerciseRepository);
    }

    @Test
    void I5_returns_the_open_exercise_covering_the_piece_date() {
        AccountingExercise exercise = exercise2026();
        LocalDate pieceDate = LocalDate.of(2026, 8, 15);
        when(accountingExerciseRepository.findCoveringDate(PROPERTY_ID, pieceDate)).thenReturn(Optional.of(exercise));

        AccountingExercise result = newService().requireOpenExerciseCovering(PROPERTY_ID, pieceDate);

        assertThat(result).isEqualTo(exercise);
    }

    @Test
    void I5_rejects_a_piece_date_no_exercise_covers() {
        LocalDate pieceDate = LocalDate.of(2027, 1, 1);
        when(accountingExerciseRepository.findCoveringDate(PROPERTY_ID, pieceDate)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().requireOpenExerciseCovering(PROPERTY_ID, pieceDate))
                .isInstanceOf(NoExerciseCoveringDateException.class);
    }

    @Test
    void I5_rejects_a_piece_date_falling_in_a_closed_exercise() {
        AccountingExercise closed = exercise2026().close(Instant.parse("2027-03-01T10:00:00Z"), EntityId.newId());
        LocalDate pieceDate = LocalDate.of(2026, 12, 1);
        when(accountingExerciseRepository.findCoveringDate(PROPERTY_ID, pieceDate)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> newService().requireOpenExerciseCovering(PROPERTY_ID, pieceDate))
                .isInstanceOf(PieceDateInClosedExerciseException.class);
    }

    private static AccountingExercise exercise2026() {
        return AccountingExercise.open(AccountingExerciseId.newId(), PROPERTY_ID, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
    }
}
