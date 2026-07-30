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

import com.architek.oikos.accounting.application.command.TransferBetweenFinancialAccountsCommand;
import com.architek.oikos.accounting.domain.exception.InsufficientFundsException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountType;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class TransferBetweenFinancialAccountsServiceTest {

    @Mock
    private FinancialAccountRepository financialAccountRepository;

    @Mock
    private FinancialJournalEntryRepository financialJournalEntryRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    private TransferBetweenFinancialAccountsService newService() {
        return new TransferBetweenFinancialAccountsService(financialAccountRepository,
                financialJournalEntryRepository, enforceExerciseOpenService);
    }

    @Test
    void a_transfer_moves_money_from_one_account_to_the_other_leaving_the_total_unchanged() {
        EntityId propertyId = EntityId.newId();
        FinancialAccountId cashId = FinancialAccountId.newId();
        FinancialAccountId bankId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount cash = FinancialAccount.create(cashId, propertyId, "Caisse", FinancialAccountType.CASH, "MAD")
                .applyEntry(FinancialEntryDirection.IN, new BigDecimal("1000"));
        FinancialAccount bank = FinancialAccount.create(bankId, propertyId, "Banque", FinancialAccountType.BANK, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(cashId)).thenReturn(Optional.of(cash));
        when(financialAccountRepository.findById(bankId)).thenReturn(Optional.of(bank));
        when(financialJournalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TransferBetweenFinancialAccountsCommand command = new TransferBetweenFinancialAccountsCommand(propertyId,
                cashId, bankId, new BigDecimal("400"), LocalDate.of(2026, 3, 1), "Depot en banque", EntityId.newId());

        newService().transfer(command);

        ArgumentCaptor<FinancialAccount> captor = ArgumentCaptor.forClass(FinancialAccount.class);
        verify(financialAccountRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        java.util.List<FinancialAccount> saved = captor.getAllValues();
        BigDecimal totalAfter = saved.get(0).getBalance().add(saved.get(1).getBalance());
        assertThat(totalAfter).isEqualByComparingTo("1000");
    }

    @Test
    void a_transfer_exceeding_the_source_balance_is_rejected() {
        EntityId propertyId = EntityId.newId();
        FinancialAccountId cashId = FinancialAccountId.newId();
        FinancialAccountId bankId = FinancialAccountId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId,
                "Exercice 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        FinancialAccount cash = FinancialAccount.create(cashId, propertyId, "Caisse", FinancialAccountType.CASH, "MAD");
        FinancialAccount bank = FinancialAccount.create(bankId, propertyId, "Banque", FinancialAccountType.BANK, "MAD");

        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(financialAccountRepository.findById(cashId)).thenReturn(Optional.of(cash));
        when(financialAccountRepository.findById(bankId)).thenReturn(Optional.of(bank));

        TransferBetweenFinancialAccountsCommand command = new TransferBetweenFinancialAccountsCommand(propertyId,
                cashId, bankId, new BigDecimal("100"), LocalDate.now(), "Depot", EntityId.newId());

        assertThatThrownBy(() -> newService().transfer(command)).isInstanceOf(InsufficientFundsException.class);
    }
}
