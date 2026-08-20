package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.OpenAccountingExerciseCommand;
import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Regression test for a real bug found via manual testing (fund calls
 * staying in DRAFT despite PostJournalEntryService calling entry.post()):
 * LedgerAccountJpaRepository.incrementBalance's bulk @Modifying update ran
 * with clearAutomatically=true but not flushAutomatically=true, so the
 * not-yet-flushed DRAFT-&gt;POSTED status update (made moments earlier, same
 * transaction) was silently discarded when the persistence context was
 * cleared - a plain Mockito unit test can't catch this, it only shows up
 * against a real Hibernate session.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PostJournalEntryPersistsStatusIntegrationTest {

    @Autowired
    private CreatePropertyUseCase createPropertyUseCase;

    @Autowired
    private OpenAccountingExerciseUseCase openAccountingExerciseUseCase;

    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @Autowired
    private PostJournalEntryUseCase postJournalEntryUseCase;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Test
    void posting_an_entry_with_several_lines_actually_persists_POSTED_status_and_updated_balances() {
        PropertyId propertyId = createPropertyUseCase.create(new CreatePropertyCommand("Journal Post Test Property", "1 rue Test", "Casablanca"));
        EntityId propertyEntityId = EntityId.of(propertyId.asUuid());
        openAccountingExerciseUseCase.open(new OpenAccountingExerciseCommand(propertyEntityId, "Exercice test",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null));

        LedgerAccountId debitAccountId = LedgerAccountId.newId();
        LedgerAccountId creditAccountId = LedgerAccountId.newId();
        ledgerAccountRepository.save(LedgerAccount.create(debitAccountId, propertyEntityId, null,
                AccountNumber.of("51610099"), "Caisse test", 5, AccountNature.BALANCE_ASSET, false, null));
        ledgerAccountRepository.save(LedgerAccount.create(creditAccountId, null, null,
                AccountNumber.of("71810099"), "Cotisations test", 7, AccountNature.INCOME, false, null));

        // Several lines (mirrors a real fund call: many debit lines, one credit line) so the
        // bulk-update-per-line loop in PostJournalEntryService actually runs more than once.
        List<CreateJournalEntryLineCommand> lines = List.of(
                new CreateJournalEntryLineCommand(debitAccountId, null, null, EntryDirection.DEBIT,
                        new BigDecimal("100.00"), "Debit 1"),
                new CreateJournalEntryLineCommand(creditAccountId, null, null, EntryDirection.CREDIT,
                        new BigDecimal("100.00"), "Credit 1"));

        JournalEntryId draftId = createJournalEntryDraftUseCase.create(new CreateJournalEntryDraftCommand(
                propertyEntityId, JournalCode.OD, null, LocalDate.of(2026, 3, 1), "Test", EntityId.newId(), lines));
        postJournalEntryUseCase.post(new PostJournalEntryCommand(draftId));

        // Re-fetch from the repository (not the use case's return value) - this is the assertion
        // that would have failed before the fix, since the in-memory returned view looked POSTED
        // even though the DB row was left as DRAFT.
        var persisted = journalEntryRepository.findById(draftId).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(persisted.getPieceNumber()).isPresent();

        assertThat(ledgerAccountRepository.findById(debitAccountId).orElseThrow().getBalance())
                .isEqualByComparingTo("100.00");
        assertThat(ledgerAccountRepository.findById(creditAccountId).orElseThrow().getBalance())
                .isEqualByComparingTo("100.00");
    }
}
