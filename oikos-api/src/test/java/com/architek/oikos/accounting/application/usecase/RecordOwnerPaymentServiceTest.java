package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.exception.UnitAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountMovementRepository;
import com.architek.oikos.accounting.domain.repository.UnitAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordOwnerPaymentServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-03-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private UnitAccountRepository unitAccountRepository;

    @Mock
    private UnitAccountMovementRepository unitAccountMovementRepository;

    @Mock
    private FinancialAccountRepository financialAccountRepository;

    @Mock
    private FinancialJournalEntryRepository financialJournalEntryRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private RecordOwnerPaymentService newService() {
        return new RecordOwnerPaymentService(unitAccountRepository, unitAccountMovementRepository,
                financialAccountRepository, financialJournalEntryRepository, enforceExerciseOpenService, CLOCK);
    }

    @Test
    void a_payment_credits_both_the_financial_account_and_the_unit_account() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        FinancialAccountId financialAccountId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        UnitAccount unitAccount = UnitAccount.reconstruct(UnitAccountId.newId(), unitId, propertyId,
                new BigDecimal("-50"), Instant.now());
        FinancialAccount financialAccount = FinancialAccount.create(financialAccountId, propertyId, "Caisse",
                FinancialAccountType.CASH, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(unitAccount));
        when(financialAccountRepository.findById(financialAccountId)).thenReturn(Optional.of(financialAccount));
        when(financialJournalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitAccountMovementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, financialAccountId,
                new BigDecimal("150"), LocalDate.of(2026, 3, 1), "Paiement janvier", EntityId.newId());

        newService().record(command);

        org.mockito.ArgumentCaptor<UnitAccount> unitAccountCaptor = org.mockito.ArgumentCaptor.forClass(UnitAccount.class);
        org.mockito.Mockito.verify(unitAccountRepository).save(unitAccountCaptor.capture());
        assertThat(unitAccountCaptor.getValue().getBalance()).isEqualByComparingTo("100");

        org.mockito.ArgumentCaptor<FinancialAccount> financialAccountCaptor = org.mockito.ArgumentCaptor.forClass(FinancialAccount.class);
        org.mockito.Mockito.verify(financialAccountRepository).save(financialAccountCaptor.capture());
        assertThat(financialAccountCaptor.getValue().getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void a_payment_for_a_unit_with_no_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.empty());

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId,
                FinancialAccountId.newId(), new BigDecimal("100"), LocalDate.now(), "Paiement", EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(UnitAccountNotFoundException.class);
    }

    @Test
    void a_payment_referencing_a_financial_account_of_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        FinancialAccountId financialAccountId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        UnitAccount unitAccount = UnitAccount.create(UnitAccountId.newId(), unitId, propertyId, Instant.now());
        FinancialAccount otherPropertyAccount = FinancialAccount.create(financialAccountId, EntityId.newId(),
                "Caisse", FinancialAccountType.CASH, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(unitAccountRepository.findByUnitId(unitId)).thenReturn(Optional.of(unitAccount));
        when(financialAccountRepository.findById(financialAccountId)).thenReturn(Optional.of(otherPropertyAccount));

        RecordOwnerPaymentCommand command = new RecordOwnerPaymentCommand(propertyId, unitId, financialAccountId,
                new BigDecimal("100"), LocalDate.now(), "Paiement", EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(FinancialAccountNotFoundException.class);
    }
}
