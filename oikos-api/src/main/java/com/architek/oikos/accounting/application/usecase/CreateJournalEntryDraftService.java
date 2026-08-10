package com.architek.oikos.accounting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.domain.exception.LedgerAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.model.LedgerAccountAuxiliaryRule;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.shared.domain.valueobject.Amount;

/**
 * Generic entry point (spec &sect;7.3, POST /ecritures): lines reference
 * LedgerAccount ids directly, chosen by the caller - unlike the higher-level
 * P1-P10 business use cases (a later phase), which resolve accounts via
 * AccountRole and call JournalEntry.draft() themselves. I5 is enforced here
 * (open exercise/period); I2/I3/treasury-binding/non-postable are enforced
 * by JournalEntry.draft() itself; I6 is enforced per line via
 * LedgerAccountAuxiliaryRule since this is the first place a resolved
 * LedgerAccount and its line are both available together.
 */
@Component
public class CreateJournalEntryDraftService implements CreateJournalEntryDraftUseCase {

    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public CreateJournalEntryDraftService(EnforceExerciseOpenService enforceExerciseOpenService,
                                           LedgerAccountRepository ledgerAccountRepository,
                                           JournalEntryRepository journalEntryRepository) {
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public JournalEntryId create(CreateJournalEntryDraftCommand command) {
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExercise(command.propertyId());
        Period period = enforceExerciseOpenService.requireOpenPeriod(exercise, command.pieceDate());

        List<JournalEntryLine> lines = command.lines().stream().map(this::toLine).toList();

        JournalEntry entry = JournalEntry.draft(JournalEntryId.newId(), command.propertyId(), exercise.getId(),
                period.getId(), command.journalCode(), command.treasuryAccountId(), command.pieceDate(),
                command.externalReference(), command.createdByUserId(), lines);

        return journalEntryRepository.save(entry).getId();
    }

    private JournalEntryLine toLine(CreateJournalEntryLineCommand lineCommand) {
        LedgerAccount account = ledgerAccountRepository.findById(lineCommand.ledgerAccountId())
                .orElseThrow(() -> new LedgerAccountNotFoundException(lineCommand.ledgerAccountId().value()));
        JournalEntryLine line = JournalEntryLine.of(JournalEntryLineId.newId(), lineCommand.ledgerAccountId(),
                lineCommand.auxiliaryUnitId(), lineCommand.auxiliaryPartyId(), lineCommand.direction(),
                Amount.of(lineCommand.amount()), lineCommand.label());
        LedgerAccountAuxiliaryRule.validate(account, line);
        return line;
    }
}
