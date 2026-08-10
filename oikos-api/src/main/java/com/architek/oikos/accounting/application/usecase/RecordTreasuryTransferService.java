package com.architek.oikos.accounting.application.usecase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.RecordTreasuryTransferCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.RecordTreasuryTransferUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.IdenticalTreasuryTransferAccountsException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * Virement interne between two of the property's own treasury accounts
 * (caisse<->banque or banque<->banque). Recorded on the OD (operations
 * diverses) journal rather than BQ/CA: JournalEntry.draft() requires a
 * TREASURY-type journal entry to carry exactly one line on its
 * treasuryAccountId, but a transfer has two treasury lines and no single
 * "the" account - OD is the Moroccan PCM catch-all journal for exactly this
 * shape of entry, with treasuryAccountId left null (both legs are still
 * fully identified via their lines, which is what the accounting-overview
 * "operations of this account" listing matches on).
 */
@Component
public class RecordTreasuryTransferService implements RecordTreasuryTransferUseCase {

    private static final String LABEL = "Virement interne";

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final TreasuryAccountResolver treasuryAccountResolver;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public RecordTreasuryTransferService(PropertyDirectoryPort propertyDirectoryPort,
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
    public JournalEntryId record(RecordTreasuryTransferCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        LedgerAccount source = treasuryAccountResolver.resolve(command.propertyId(), command.sourceAccountId(),
                Set.of(AccountRole.BANK, AccountRole.CASH));
        LedgerAccount destination = treasuryAccountResolver.resolve(command.propertyId(),
                command.destinationAccountId(), Set.of(AccountRole.BANK, AccountRole.CASH));
        if (source.getId().equals(destination.getId())) {
            throw new IdenticalTreasuryTransferAccountsException(source.getId());
        }

        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(destination.getId(), null, null, EntryDirection.DEBIT,
                        command.amount(), LABEL),
                new CreateJournalEntryLineCommand(source.getId(), null, null, EntryDirection.CREDIT,
                        command.amount(), LABEL));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), JournalCode.OD, null, command.pieceDate(), command.description(),
                command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
