package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.RecordUnitAccountRegularizationCommand;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordUnitAccountRegularizationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private UnitAccountMovementRepository unitAccountMovementRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private RecordUnitAccountRegularizationService newService() {
        return new RecordUnitAccountRegularizationService(unitAccountRepository, unitAccountMovementRepository,
                enforceExerciseOpenService, CLOCK);
    }

    @Test
    void a_credit_regularization_increases_the_unit_account_balance() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        UnitAccount unitAccount = UnitAccount.reconstruct(UnitAccountId.newId(), unitId, propertyId,
                new BigDecimal("-100"), Instant.now());
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 12, 31), null);

        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(unitAccount));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(unitAccountMovementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordUnitAccountRegularizationCommand command = new RecordUnitAccountRegularizationCommand(unitId,
                new BigDecimal("100"), UnitAccountMovementDirection.CREDIT, "Remise exceptionnelle",
                "Geste commercial suite a un incident");

        newService().record(command);

        ArgumentCaptor<UnitAccount> captor = ArgumentCaptor.forClass(UnitAccount.class);
        verify(unitAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void a_regularization_for_a_unit_with_no_account_is_rejected() {
        EntityId unitId = EntityId.newId();
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        RecordUnitAccountRegularizationCommand command = new RecordUnitAccountRegularizationCommand(unitId,
                new BigDecimal("50"), UnitAccountMovementDirection.CREDIT, "Remise", "Motif");

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(UnitAccountNotFoundException.class);
    }
}
