package com.architek.oikos.accounting.application.usecase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.RecordBankChargeCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.RecordBankChargeUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/** Same composition shape as P1-P5's posting services. */
@Component
public class RecordBankChargeService implements RecordBankChargeUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final TreasuryAccountResolver treasuryAccountResolver;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public RecordBankChargeService(PropertyDirectoryPort propertyDirectoryPort,
                                    TreasuryAccountResolver treasuryAccountResolver,
                                    CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                    PostJournalEntryUseCase postJournalEntryUseCase) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.treasuryAccountResolver = treasuryAccountResolver;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId record(RecordBankChargeCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        LedgerAccount bankAccount = treasuryAccountResolver.resolve(command.propertyId(), command.bankAccountId(),
                Set.of(AccountRole.BANK));

        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(command.ledgerAccountId(), null, null, EntryDirection.DEBIT,
                        command.amount(), "Frais bancaires"),
                new CreateJournalEntryLineCommand(bankAccount.getId(), null, null, EntryDirection.CREDIT,
                        command.amount(), "Frais bancaires"));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), JournalCode.BQ, bankAccount.getId(), command.pieceDate(), command.description(),
                command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
