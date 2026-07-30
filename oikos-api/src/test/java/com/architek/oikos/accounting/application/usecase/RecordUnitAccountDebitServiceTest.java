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

import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordUnitAccountDebitServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-02-05T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private UnitAccountMovementRepository unitAccountMovementRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private RecordUnitAccountDebitService newService() {
        return new RecordUnitAccountDebitService(unitAccountRepository, unitAccountMovementRepository,
                enforceExerciseOpenService, CLOCK);
    }

    @Test
    void recording_a_debit_posts_a_fund_call_movement_and_decreases_the_balance() {
        EntityId propertyId = EntityId.newId();
        UnitAccountId unitAccountId = UnitAccountId.newId();
        EntityId installmentId = EntityId.newId();
        UnitAccount unitAccount = UnitAccount.create(unitAccountId, EntityId.newId(), propertyId, Instant.now());
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 12, 31), null);

        when(unitAccountRepository.findById(unitAccountId)).thenReturn(Optional.of(unitAccount));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(unitAccountMovementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().recordDebit(unitAccountId.value(), new BigDecimal("200"), "Appel de cotisation", installmentId);

        ArgumentCaptor<UnitAccountMovement> movementCaptor = ArgumentCaptor.forClass(UnitAccountMovement.class);
        verify(unitAccountMovementRepository).save(movementCaptor.capture());
        assertThat(movementCaptor.getValue().getType()).isEqualTo(UnitAccountMovementType.FUND_CALL);
        assertThat(movementCaptor.getValue().getBusinessReference()).isEqualTo(installmentId.toString());

        ArgumentCaptor<UnitAccount> accountCaptor = ArgumentCaptor.forClass(UnitAccount.class);
        verify(unitAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo("-200");
    }

    @Test
    void recording_a_debit_for_an_unknown_unit_account_is_rejected() {
        UnitAccountId unitAccountId = UnitAccountId.newId();
        when(unitAccountRepository.findById(unitAccountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().recordDebit(unitAccountId.value(), BigDecimal.TEN, "Appel",
                EntityId.newId())).isInstanceOf(UnitAccountNotFoundException.class);
    }
}
