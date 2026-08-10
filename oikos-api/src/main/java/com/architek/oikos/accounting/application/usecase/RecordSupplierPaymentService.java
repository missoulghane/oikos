package com.architek.oikos.accounting.application.usecase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.RecordSupplierPaymentCommand;
import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.RecordSupplierPaymentUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * Direct supplier payment (Partie 2: merges what used to be P4's accrual
 * and P5's settlement, since removing the supplier party left the accrual
 * step with no debt to track) - a simple two-line entry, same shape as
 * RecordBankChargeService: debit the caller-chosen charge account, credit
 * the caller-chosen treasury account (Partie 3, resolved/validated by
 * TreasuryAccountResolver). Also persists an Expense (no supplier party
 * field, see domain model) so the payment shows up in the "Depenses" list.
 */
@Component
public class RecordSupplierPaymentService implements RecordSupplierPaymentUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final TreasuryAccountResolver treasuryAccountResolver;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;
    private final ExpenseRepository expenseRepository;

    public RecordSupplierPaymentService(PropertyDirectoryPort propertyDirectoryPort,
                                         TreasuryAccountResolver treasuryAccountResolver,
                                         CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                         PostJournalEntryUseCase postJournalEntryUseCase,
                                         ExpenseRepository expenseRepository) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.treasuryAccountResolver = treasuryAccountResolver;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional
    public ExpenseView record(RecordSupplierPaymentCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        LedgerAccount treasuryAccount = treasuryAccountResolver.resolve(command.propertyId(),
                command.treasuryAccountId(), Set.of(AccountRole.BANK, AccountRole.CASH));

        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(command.ledgerAccountId(), null, null, EntryDirection.DEBIT,
                        command.amount(), "Reglement fournisseur"),
                new CreateJournalEntryLineCommand(treasuryAccount.getId(), null, null, EntryDirection.CREDIT,
                        command.amount(), "Reglement fournisseur"));

        JournalCode journalCode = treasuryAccount.getRole().filter(AccountRole.CASH::equals).isPresent()
                ? JournalCode.CA : JournalCode.BQ;

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), journalCode, treasuryAccount.getId(), command.pieceDate(),
                command.externalReference(), command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));

        Expense expense = Expense.create(ExpenseId.newId(), command.propertyId(), command.pieceDate(),
                command.ledgerAccountId(), Amount.of(command.amount()), command.description(),
                command.externalReference(), draftId);
        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseView.from(savedExpense);
    }
}
