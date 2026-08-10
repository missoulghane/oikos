package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.FundCallLineCommand;
import com.architek.oikos.accounting.application.command.PostFundCallJournalEntryCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostFundCallJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Composes the generic engine (CreateJournalEntryDraftUseCase +
 * PostJournalEntryUseCase) rather than talking to JournalEntryRepository
 * directly, so I1/I2/I5/I7 stay enforced in exactly one place.
 */
@Component
public class PostFundCallJournalEntryService implements PostFundCallJournalEntryUseCase {

    private final LedgerAccountRepository ledgerAccountRepository;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public PostFundCallJournalEntryService(LedgerAccountRepository ledgerAccountRepository,
                                            CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                            PostJournalEntryUseCase postJournalEntryUseCase) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId post(PostFundCallJournalEntryCommand command) {
        LedgerAccount duesIncomeAccount = ledgerAccountRepository.findGlobalByRole(AccountRole.DUES_INCOME)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.DUES_INCOME, command.propertyId()));

        List<CreateJournalEntryLineCommand> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (FundCallLineCommand line : command.lines()) {
            LedgerAccountId receivableAccountId = resolveUnitReceivableAccount(command.propertyId(), line.unitId());
            lines.add(new CreateJournalEntryLineCommand(receivableAccountId, line.unitId(), null, EntryDirection.DEBIT,
                    line.amount(), "Appel de fonds"));
            total = total.add(line.amount());
        }
        lines.add(new CreateJournalEntryLineCommand(duesIncomeAccount.getId(), null, null, EntryDirection.CREDIT,
                total, "Cotisations des coproprietaires"));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), JournalCode.VT, null, command.pieceDate(), command.externalReference(),
                command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }

    private LedgerAccountId resolveUnitReceivableAccount(EntityId propertyId, EntityId unitId) {
        return ledgerAccountRepository.findByPropertyIdAndUnitIdAndRole(propertyId, unitId, AccountRole.UNIT_RECEIVABLE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.UNIT_RECEIVABLE, propertyId, unitId))
                .getId();
    }
}
