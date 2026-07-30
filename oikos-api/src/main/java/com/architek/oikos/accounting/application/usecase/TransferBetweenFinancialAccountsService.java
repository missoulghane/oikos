package com.architek.oikos.accounting.application.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.TransferBetweenFinancialAccountsCommand;
import com.architek.oikos.accounting.application.port.in.TransferBetweenFinancialAccountsUseCase;
import com.architek.oikos.accounting.domain.exception.FinancialAccountNotFoundException;
import com.architek.oikos.accounting.domain.exception.InsufficientFundsException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.repository.FinancialAccountRepository;
import com.architek.oikos.accounting.domain.repository.FinancialJournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Spec &sect;14: a transfer between two of the property's financial accounts
 * creates one OUT entry and one IN entry (same amount), linked by a shared
 * transferReference - the property's total treasury is unchanged.
 */
@Component
public class TransferBetweenFinancialAccountsService implements TransferBetweenFinancialAccountsUseCase {

    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialJournalEntryRepository financialJournalEntryRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;

    public TransferBetweenFinancialAccountsService(FinancialAccountRepository financialAccountRepository,
                                                     FinancialJournalEntryRepository financialJournalEntryRepository,
                                                     EnforceExerciseOpenService enforceExerciseOpenService) {
        this.financialAccountRepository = financialAccountRepository;
        this.financialJournalEntryRepository = financialJournalEntryRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
    }

    @Override
    @Transactional
    public void transfer(TransferBetweenFinancialAccountsCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        FinancialAccount fromAccount = findOwnedAccount(command.fromAccountId(), command.propertyId());
        FinancialAccount toAccount = findOwnedAccount(command.toAccountId(), command.propertyId());

        if (fromAccount.getBalance().compareTo(command.amount()) < 0) {
            throw new InsufficientFundsException(fromAccount.getId(), fromAccount.getBalance(), command.amount());
        }

        String transferReference = UUID.randomUUID().toString();
        Amount amount = Amount.of(command.amount());

        financialJournalEntryRepository.save(FinancialJournalEntry.create(FinancialJournalEntryId.newId(),
                exercise.getId(), fromAccount.getId(), command.date(), FinancialEntryType.TRANSFER_OUT,
                FinancialEntryDirection.OUT, amount, command.label(), transferReference, command.createdByUserId()));
        financialJournalEntryRepository.save(FinancialJournalEntry.create(FinancialJournalEntryId.newId(),
                exercise.getId(), toAccount.getId(), command.date(), FinancialEntryType.TRANSFER_IN,
                FinancialEntryDirection.IN, amount, command.label(), transferReference, command.createdByUserId()));

        financialAccountRepository.save(fromAccount.applyEntry(FinancialEntryDirection.OUT, command.amount()));
        financialAccountRepository.save(toAccount.applyEntry(FinancialEntryDirection.IN, command.amount()));
    }

    private FinancialAccount findOwnedAccount(FinancialAccountId id, EntityId propertyId) {
        return financialAccountRepository.findById(id)
                .filter(account -> account.getPropertyId().equals(propertyId))
                .orElseThrow(() -> new FinancialAccountNotFoundException(id));
    }
}
