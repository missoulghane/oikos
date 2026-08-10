package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.PostOwnerPaymentJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.PostOwnerPaymentJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

/**
 * Composes the generic engine (CreateJournalEntryDraftUseCase +
 * PostJournalEntryUseCase) rather than talking to JournalEntryRepository
 * directly, so I1/I2/I5/I7 stay enforced in exactly one place - same shape
 * as PostFundCallJournalEntryService (P1).
 */
@Component
public class PostOwnerPaymentJournalEntryService implements PostOwnerPaymentJournalEntryUseCase {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final TreasuryAccountResolver treasuryAccountResolver;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public PostOwnerPaymentJournalEntryService(LedgerAccountRepository ledgerAccountRepository,
                                                TreasuryAccountResolver treasuryAccountResolver,
                                                CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                                PostJournalEntryUseCase postJournalEntryUseCase) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.treasuryAccountResolver = treasuryAccountResolver;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId post(PostOwnerPaymentJournalEntryCommand command) {
        LedgerAccount treasuryAccount = treasuryAccountResolver.resolve(command.propertyId(),
                command.treasuryAccountId(), Set.of(AccountRole.BANK, AccountRole.CASH));

        BigDecimal totalAmount = command.imputedAmount().add(command.advanceAmount());
        List<CreateJournalEntryLineCommand> lines = new ArrayList<>();
        lines.add(new CreateJournalEntryLineCommand(treasuryAccount.getId(), command.unitId(), null,
                EntryDirection.DEBIT, totalAmount, "Reglement coproprietaire"));

        if (command.imputedAmount().signum() > 0) {
            LedgerAccountId receivableAccountId = ledgerAccountRepository
                    .findByPropertyIdAndUnitIdAndRole(command.propertyId(), command.unitId(), AccountRole.UNIT_RECEIVABLE)
                    .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_RECEIVABLE,
                            command.propertyId(), command.unitId()))
                    .getId();
            lines.add(new CreateJournalEntryLineCommand(receivableAccountId, command.unitId(), null,
                    EntryDirection.CREDIT, command.imputedAmount(), "Imputation appel(s) de fonds"));
        }

        if (command.advanceAmount().signum() > 0) {
            LedgerAccountId advanceAccountId = ledgerAccountRepository
                    .findGlobalByRole(AccountRole.UNIT_ADVANCE)
                    .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_ADVANCE, command.propertyId()))
                    .getId();
            lines.add(new CreateJournalEntryLineCommand(advanceAccountId, command.unitId(), null,
                    EntryDirection.CREDIT, command.advanceAmount(), "Avance"));
        }

        JournalCode journalCode = treasuryAccount.getRole().filter(AccountRole.CASH::equals).isPresent()
                ? JournalCode.CA : JournalCode.BQ;

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), journalCode, treasuryAccount.getId(), command.pieceDate(),
                command.externalReference(), command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
