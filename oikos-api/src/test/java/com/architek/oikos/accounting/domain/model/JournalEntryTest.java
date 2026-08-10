package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.exception.InsufficientJournalEntryLinesException;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotDraftException;
import com.architek.oikos.accounting.domain.exception.NonPostableJournalException;
import com.architek.oikos.accounting.domain.exception.TreasuryJournalLineMismatchException;
import com.architek.oikos.accounting.domain.exception.UnbalancedJournalEntryException;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class JournalEntryTest {

    private final EntityId propertyId = EntityId.newId();
    private final AccountingExerciseId exerciseId = AccountingExerciseId.newId();
    private final PeriodId periodId = PeriodId.newId();
    private final EntityId createdByUserId = EntityId.newId();
    private final LedgerAccountId debitAccountId = LedgerAccountId.newId();
    private final LedgerAccountId creditAccountId = LedgerAccountId.newId();

    private JournalEntryLine line(LedgerAccountId accountId, EntryDirection direction, String amount) {
        return JournalEntryLine.of(JournalEntryLineId.newId(), accountId, null, null, direction,
                Amount.of(new BigDecimal(amount)), "Test line");
    }

    private JournalEntry newDraft(List<JournalEntryLine> lines) {
        return JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId, JournalCode.OD, null,
                LocalDate.of(2026, 8, 1), null, createdByUserId, lines);
    }

    @Test
    void I1_balancedEntryIsPostedSuccessfully() {
        JournalEntry entry = newDraft(List.of(
                line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                line(creditAccountId, EntryDirection.CREDIT, "100.00")));

        JournalEntry posted = entry.post(1);

        assertThat(posted.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(posted.getPieceNumber()).contains(1);
    }

    @Test
    void I1_unbalancedEntryIsRejectedAtPosting() {
        JournalEntry entry = newDraft(List.of(
                line(debitAccountId, EntryDirection.DEBIT, "150.00"),
                line(creditAccountId, EntryDirection.CREDIT, "100.00")));

        assertThatThrownBy(() -> entry.post(1))
                .isInstanceOf(UnbalancedJournalEntryException.class)
                .satisfies(ex -> assertThat(((UnbalancedJournalEntryException) ex).getDifference())
                        .isEqualByComparingTo("50.00"));
    }

    @Test
    void I1_draftEntryMayBeTransientlyUnbalanced() {
        JournalEntry entry = newDraft(List.of(
                line(debitAccountId, EntryDirection.DEBIT, "150.00"),
                line(creditAccountId, EntryDirection.CREDIT, "100.00")));

        assertThat(entry.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
    }

    @Test
    void I2_entryWithFewerThanTwoLinesIsRejected() {
        assertThatThrownBy(() -> newDraft(List.of(line(debitAccountId, EntryDirection.DEBIT, "100.00"))))
                .isInstanceOf(InsufficientJournalEntryLinesException.class);
    }

    @Test
    void I4_postedEntryCannotBePostedAgain() {
        JournalEntry posted = newDraft(List.of(
                line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                line(creditAccountId, EntryDirection.CREDIT, "100.00"))).post(1);

        assertThatThrownBy(() -> posted.post(2)).isInstanceOf(JournalEntryNotDraftException.class);
    }

    @Test
    void a_non_postable_journal_is_rejected_at_draft() {
        assertThatThrownBy(() -> JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.AN, null, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                        line(creditAccountId, EntryDirection.CREDIT, "100.00"))))
                .isInstanceOf(NonPostableJournalException.class);
    }

    @Test
    void a_treasury_journal_entry_requires_a_treasury_account_id() {
        assertThatThrownBy(() -> JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.BQ, null, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                        line(creditAccountId, EntryDirection.CREDIT, "100.00"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_treasury_journal_entry_needs_exactly_one_line_on_the_treasury_account() {
        assertThatThrownBy(() -> JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.BQ, debitAccountId, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(creditAccountId, EntryDirection.DEBIT, "100.00"),
                        line(creditAccountId, EntryDirection.CREDIT, "100.00"))))
                .isInstanceOf(TreasuryJournalLineMismatchException.class);
    }

    @Test
    void a_treasury_journal_entry_with_exactly_one_matching_line_is_accepted() {
        JournalEntry entry = JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.BQ, debitAccountId, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                        line(creditAccountId, EntryDirection.CREDIT, "100.00")));

        assertThat(entry.getTreasuryAccountId()).contains(debitAccountId);
    }

    @Test
    void a_non_treasury_journal_entry_must_not_carry_a_treasury_account_id() {
        assertThatThrownBy(() -> JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.OD, debitAccountId, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                        line(creditAccountId, EntryDirection.CREDIT, "100.00"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void I7_pieceNumberMustBePositive() {
        JournalEntry entry = newDraft(List.of(
                line(debitAccountId, EntryDirection.DEBIT, "100.00"),
                line(creditAccountId, EntryDirection.CREDIT, "100.00")));

        assertThatThrownBy(() -> entry.post(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
