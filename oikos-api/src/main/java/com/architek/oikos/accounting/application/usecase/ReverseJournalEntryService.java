package com.architek.oikos.accounting.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.ReverseJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.ReverseJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotPostedException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;

/**
 * P10 (spec &sect;10): the original entry is never touched beyond its status -
 * its lines stay, and a mirror entry (same accounts, flipped directions,
 * originalEntryId set) cancels it in the ledger. The mirror goes through
 * PostJournalEntryUseCase like any entry: piece number, balances.
 *
 * <p><b>Date of the mirror.</b> While the original's exercise is still open,
 * the mirror is dated like the original: both land in the same exercise and
 * cancel out there, leaving its result untouched. Once that exercise is
 * closed it can no longer receive anything, so the mirror is dated
 * command.reversalDate() and lands in whichever open exercise covers that day
 * - the correction then shows in the following exercise, as it should.
 */
@Component
public class ReverseJournalEntryService implements ReverseJournalEntryUseCase {

    private static final String REFERENCE_PREFIX = "EXTOURNE ";
    private static final int REFERENCE_MAX_LENGTH = 200;

    private final JournalEntryRepository journalEntryRepository;
    private final AccountingExerciseRepository accountingExerciseRepository;
    private final EnforceExerciseOpenService enforceExerciseOpenService;
    private final PostJournalEntryUseCase postJournalEntryUseCase;

    public ReverseJournalEntryService(JournalEntryRepository journalEntryRepository,
                                       AccountingExerciseRepository accountingExerciseRepository,
                                       EnforceExerciseOpenService enforceExerciseOpenService,
                                       PostJournalEntryUseCase postJournalEntryUseCase) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountingExerciseRepository = accountingExerciseRepository;
        this.enforceExerciseOpenService = enforceExerciseOpenService;
        this.postJournalEntryUseCase = postJournalEntryUseCase;
    }

    @Override
    @Transactional
    public JournalEntryId reverse(ReverseJournalEntryCommand command) {
        JournalEntry original = journalEntryRepository.findById(command.entryId())
                .filter(entry -> entry.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));
        if (original.getStatus() != JournalEntryStatus.POSTED) {
            throw new JournalEntryNotPostedException(original.getId(), original.getStatus());
        }

        boolean originalExerciseOpen = accountingExerciseRepository.findById(original.getExerciseId())
                .map(AccountingExercise::isOpen)
                .orElse(false);
        LocalDate pieceDate = originalExerciseOpen ? original.getPieceDate() : command.reversalDate();
        AccountingExercise exercise = enforceExerciseOpenService.requireOpenExerciseCovering(command.propertyId(),
                pieceDate);

        List<JournalEntryLineId> lineIds = IntStream.range(0, original.getLines().size())
                .mapToObj(index -> JournalEntryLineId.newId())
                .toList();
        JournalEntry mirror = JournalEntry.draft(JournalEntryId.newId(), original.getPropertyId(), exercise.getId(),
                original.getJournalCode(), original.getTreasuryAccountId().orElse(null), pieceDate,
                reference(original), command.reversedByUserId(), original.mirrorLinesForReversal(lineIds),
                original.getId());
        JournalEntryId mirrorId = journalEntryRepository.save(mirror).getId();
        postJournalEntryUseCase.post(new PostJournalEntryCommand(mirrorId));

        journalEntryRepository.save(original.markReversed());
        return mirrorId;
    }

    private static String reference(JournalEntry original) {
        String reference = REFERENCE_PREFIX + original.getExternalReference().orElse(original.getId().toString());
        return reference.length() <= REFERENCE_MAX_LENGTH ? reference : reference.substring(0, REFERENCE_MAX_LENGTH);
    }
}
