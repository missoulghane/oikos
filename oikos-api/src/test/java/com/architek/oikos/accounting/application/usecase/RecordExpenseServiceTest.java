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

import com.architek.oikos.accounting.application.command.RecordExpenseCommand;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private FinancialAccountRepository financialAccountRepository;

    @Mock
    private FinancialJournalEntryRepository financialJournalEntryRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private RecordExpenseService newService() {
        return new RecordExpenseService(expenseRepository, financialAccountRepository,
                financialJournalEntryRepository, enforceExerciseOpenService);
    }

    @Test
    void recording_an_expense_decreases_the_financial_account_balance() {
        EntityId propertyId = EntityId.newId();
        FinancialAccountId financialAccountId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount account = FinancialAccount.create(financialAccountId, propertyId, "Caisse",
                FinancialAccountType.CASH, "MAD").applyEntry(com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection.IN,
                new BigDecimal("1000"));

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(financialAccountId)).thenReturn(Optional.of(account));
        when(financialJournalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordExpenseCommand command = new RecordExpenseCommand(propertyId, financialAccountId,
                LocalDate.of(2026, 3, 1), "Gardiennage", "Securitas", new BigDecimal("300"), "Mois de mars", null,
                EntityId.newId());

        newService().record(command);

        ArgumentCaptor<FinancialAccount> captor = ArgumentCaptor.forClass(FinancialAccount.class);
        verify(financialAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("700");
    }

    @Test
    void recording_an_expense_on_a_financial_account_of_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        FinancialAccountId financialAccountId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount otherPropertyAccount = FinancialAccount.create(financialAccountId, EntityId.newId(),
                "Caisse", FinancialAccountType.CASH, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(financialAccountId)).thenReturn(Optional.of(otherPropertyAccount));

        RecordExpenseCommand command = new RecordExpenseCommand(propertyId, financialAccountId, LocalDate.now(),
                "Gardiennage", "Securitas", new BigDecimal("300"), null, null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(FinancialAccountNotFoundException.class);
    }
}
