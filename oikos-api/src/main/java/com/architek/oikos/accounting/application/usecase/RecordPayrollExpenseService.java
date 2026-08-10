package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.RecordPayrollExpenseCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.RecordPayrollExpenseUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;

/** Same composition shape as P1-P5/P7's posting services. */
@Component
public class RecordPayrollExpenseService implements RecordPayrollExpenseUseCase {

    private final PropertyDirectoryPort propertyDirectoryPort;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public RecordPayrollExpenseService(PropertyDirectoryPort propertyDirectoryPort,
                                        LedgerAccountRepository ledgerAccountRepository,
                                        CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase,
                                        PostJournalEntryUseCase postJournalEntryUseCase) {
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.createJournalEntryDraftUseCase = createJournalEntryDraftUseCase;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId record(RecordPayrollExpenseCommand command) {
        if (!propertyDirectoryPort.exists(command.propertyId())) {
            throw new PropertyNotFoundException(command.propertyId());
        }

        LedgerAccountId staffPayableAccountId = ledgerAccountRepository.findGlobalByRole(AccountRole.STAFF_PAYABLE)
                .orElseThrow(() -> new AccountRoleNotConfiguredException(AccountRole.STAFF_PAYABLE, command.propertyId()))
                .getId();

        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(command.ledgerAccountId(), null, null, EntryDirection.DEBIT,
                        command.amount(), "Charge de personnel"),
                new CreateJournalEntryLineCommand(staffPayableAccountId, null, null, EntryDirection.CREDIT,
                        command.amount(), "Charge de personnel"));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                command.propertyId(), JournalCode.OD, null, command.date(), command.description(),
                command.createdByUserId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));
        return draftId;
    }
}
