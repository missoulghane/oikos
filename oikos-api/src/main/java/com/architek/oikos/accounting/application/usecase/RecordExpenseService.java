package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordExpenseCommand;
import com.architek.oikos.accounting.application.port.in.RecordExpenseUseCase;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * Spec &sect;6: validating an expense creates its triggering OUT journal
 * entry and decreases the financial account's balance, same transaction.
 * Every recorded expense maps to the coarse-grained OTHER_EXPENSE journal
 * type - the expense's own free-text category/provider is the fine-grained
 * detail the journal doesn't need to distinguish further.
 */
@Component
public class RecordExpenseService implements RecordExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialJournalEntryRepository financialJournalEntryRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;

    public RecordExpenseService(ExpenseRepository expenseRepository,
                                 FinancialAccountRepository financialAccountRepository,
                                 FinancialJournalEntryRepository financialJournalEntryRepository,
                                 EnforceExerciseOpenService enforceExerciseOpenService) {
        this.expenseRepository = expenseRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.financialJournalEntryRepository = financialJournalEntryRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
    }

    @Override
    @Transactional
    public ExpenseId record(RecordExpenseCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        FinancialAccount account = financialAccountRepository.findById(command.financialAccountId())
                .filter(a -> a.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new FinancialAccountNotFoundException(command.financialAccountId()));

        Amount amount = Amount.of(command.amount());
        FinancialJournalEntry entry = FinancialJournalEntry.create(FinancialJournalEntryId.newId(), exercise.getId(),
                account.getId(), command.date(), FinancialEntryType.OTHER_EXPENSE, FinancialEntryDirection.OUT,
                amount, command.category() + " - " + command.provider(), null, command.createdByUserId());
        FinancialJournalEntry savedEntry = financialJournalEntryRepository.save(entry);

        financialAccountRepository.save(account.applyEntry(FinancialEntryDirection.OUT, command.amount()));

        Expense expense = Expense.create(ExpenseId.newId(), exercise.getId(), account.getId(), command.date(),
                command.category(), command.provider(), amount, command.description(), command.receiptReference(),
                savedEntry.getId());
        return expenseRepository.save(expense).getId();
    }
}
