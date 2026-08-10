package com.architek.oikos.accounting.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
import com.architek.oikos.accounting.domain.exception.LedgerAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;

/**
 * Spec &sect;7.3, POST /ecritures/{id}/validation: allocates the piece
 * number (I7), applies I1 (JournalEntry.post), then updates each touched
 * account's persisted balance (Partie: solde persiste) - this is the single
 * choke point every write flow (P1-P7, the generic entries endpoint) goes
 * through to reach POSTED, so it is the only place this needs to happen.
 * Reversal (JournalEntry.markReversed()) is not wired to any use case in
 * production, so an entry never leaves POSTED once reached here - no
 * decrement path is needed (yet).
 */
@Component
public class PostJournalEntryService implements PostJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;

    public PostJournalEntryService(JournalEntryRepository journalEntryRepository,
                                    LedgerAccountRepository ledgerAccountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
    }

    @Override
    @Transactional
    public JournalEntryView post(PostJournalEntryCommand command) {
        JournalEntry entry = journalEntryRepository.findById(command.entryId())
                .orElseThrow(() -> new JournalEntryNotFoundException(command.entryId()));
        int pieceNumber = journalEntryRepository.nextPieceNumber(entry.getPropertyId(), entry.getExerciseId(),
                entry.getJournalCode());
        JournalEntry posted = entry.post(pieceNumber);
        JournalEntry saved = journalEntryRepository.save(posted);

        for (JournalEntryLine line : saved.getLines()) {
            LedgerAccount account = ledgerAccountRepository.findById(line.getLedgerAccountId())
                    .orElseThrow(() -> new LedgerAccountNotFoundException(line.getLedgerAccountId().value()));
            BigDecimal amount = line.getAmount().value();
            BigDecimal delta = line.getDirection() == account.getNormalSide() ? amount : amount.negate();
            ledgerAccountRepository.incrementBalance(account.getId(), delta);
        }

        return JournalEntryView.from(saved);
    }
}
