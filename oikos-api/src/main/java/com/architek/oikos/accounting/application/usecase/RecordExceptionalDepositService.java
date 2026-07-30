package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.RecordExceptionalDepositCommand;
import com.architek.oikos.accounting.application.port.in.RecordExceptionalDepositUseCase;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * Bootstraps or tops up a financial account with money that did not come
 * through an owner payment (opening balance when onboarding an existing
 * property, insurance payout, grant...). Unlike a payment, never touches a
 * unit account - purely a treasury-side IN entry.
 */
@Component
public class RecordExceptionalDepositService implements RecordExceptionalDepositUseCase {

    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialJournalEntryRepository financialJournalEntryRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;

    public RecordExceptionalDepositService(FinancialAccountRepository financialAccountRepository,
                                            FinancialJournalEntryRepository financialJournalEntryRepository,
                                            EnforceExerciseOpenService enforceExerciseOpenService) {
        this.financialAccountRepository = financialAccountRepository;
        this.financialJournalEntryRepository = financialJournalEntryRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
    }

    @Override
    @Transactional
    public void record(RecordExceptionalDepositCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        FinancialAccount account = financialAccountRepository.findById(command.financialAccountId())
                .filter(a -> a.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new FinancialAccountNotFoundException(command.financialAccountId()));

        Amount amount = Amount.of(command.amount());
        FinancialJournalEntry entry = FinancialJournalEntry.create(FinancialJournalEntryId.newId(), exercise.getId(),
                account.getId(), command.date(), FinancialEntryType.EXCEPTIONAL_DEPOSIT, FinancialEntryDirection.IN,
                amount, command.label(), null, command.createdByUserId());
        financialJournalEntryRepository.save(entry);

        financialAccountRepository.save(account.applyEntry(FinancialEntryDirection.IN, command.amount()));
    }
}
