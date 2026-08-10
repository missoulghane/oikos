package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostAdvanceRegularizationJournalEntryCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostAdvanceRegularizationJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;

/**
 * Regularisation: DEBIT the (global, collective) advance account / CREDIT
 * the unit's own receivable account, for the amount the caller already
 * determined imputable (installment module's FIFO allocation). Posted on
 * OD (operations diverses) rather than a treasury journal: neither line
 * touches a treasury account, this is a pure reallocation between two
 * balance-sheet accounts, same journal-code reasoning as
 * RecordTreasuryTransferService.
 */
@Component
public class PostAdvanceRegularizationJournalEntryService implements PostAdvanceRegularizationJournalEntryUseCase {

    private static final String LABEL = "Regularisation avance";

    private final LedgerAccountRepository ledgerAccountRepository;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public PostAdvanceRegularizationJournalEntryService(LedgerAccountRepository ledgerAccountRepository,
                                                         CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                                         PostJournalEntryUseCase postJournalEntryUseCase) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId post(PostAdvanceRegularizationJournalEntryCommand command) {
        LedgerAccount advanceAccount = ledgerAccountRepository.findGlobalByRole(AccountRole.UNIT_ADVANCE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_ADVANCE, command.propertyId()));
        LedgerAccount receivableAccount = ledgerAccountRepository
                .findByPropertyIdAndUnitIdAndRole(command.propertyId(), command.unitId(), AccountRole.UNIT_RECEIVABLE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_RECEIVABLE,
                        command.propertyId(), command.unitId()));

        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(advanceAccount.getId(), command.unitId(), null,
                        EntryDirection.DEBIT, command.amount(), LABEL),
                new CreateJournalEntryLineCommand(receivableAccount.getId(), command.unitId(), null,
                        EntryDirection.CREDIT, command.amount(), LABEL));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), JournalCode.OD, null, command.pieceDate(), LABEL, command.createdByUserId(),
                lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
