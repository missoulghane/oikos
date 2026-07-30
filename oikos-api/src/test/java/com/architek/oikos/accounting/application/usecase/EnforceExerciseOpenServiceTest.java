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
}
