package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.RecordExceptionalDepositCommand;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordExceptionalDepositServiceTest {

    @Mock
    private FinancialAccountRepository financialAccountRepository;

    @Mock
    private FinancialJournalEntryRepository financialJournalEntryRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private RecordExceptionalDepositService newService() {
        return new RecordExceptionalDepositService(financialAccountRepository, financialJournalEntryRepository,
                enforceExerciseOpenService);
    }

    @Test
    void a_deposit_increases_the_account_balance_and_creates_an_in_journal_entry() {
        EntityId propertyId = EntityId.newId();
        FinancialAccountId cashId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount cash = FinancialAccount.create(cashId, propertyId, "Caisse", FinancialAccountType.CASH, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(cashId)).thenReturn(Optional.of(cash));

        RecordExceptionalDepositCommand command = new RecordExceptionalDepositCommand(propertyId, cashId,
                new BigDecimal("5000"), LocalDate.of(2026, 1, 5), "Solde d'ouverture", EntityId.newId());

        newService().record(command);

        ArgumentCaptor<FinancialJournalEntry> entryCaptor = ArgumentCaptor.forClass(FinancialJournalEntry.class);
        verify(financialJournalEntryRepository).save(entryCaptor.capture());
        assertThat(entryCaptor.getValue().getType()).isEqualTo(FinancialEntryType.EXCEPTIONAL_DEPOSIT);
        assertThat(entryCaptor.getValue().getDirection()).isEqualTo(FinancialEntryDirection.IN);
        assertThat(entryCaptor.getValue().getAmount().value()).isEqualByComparingTo("5000");

        ArgumentCaptor<FinancialAccount> accountCaptor = ArgumentCaptor.forClass(FinancialAccount.class);
        verify(financialAccountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getBalance()).isEqualByComparingTo("5000");
    }

    @Test
    void a_deposit_on_a_financial_account_of_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId otherPropertyId = EntityId.newId();
        FinancialAccountId cashId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount cash = FinancialAccount.create(cashId, otherPropertyId, "Caisse", FinancialAccountType.CASH,
                "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(cashId)).thenReturn(Optional.of(cash));

        RecordExceptionalDepositCommand command = new RecordExceptionalDepositCommand(propertyId, cashId,
                new BigDecimal("100"), LocalDate.now(), "Depot", EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(FinancialAccountNotFoundException.class);
    }
}
