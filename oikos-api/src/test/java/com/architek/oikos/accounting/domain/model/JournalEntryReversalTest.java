package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.exception.JournalEntryNotPostedException;
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

class JournalEntryReversalTest {

    private final EntityId propertyId = EntityId.newId();
    private final AccountingExerciseId exerciseId = AccountingExerciseId.newId();
    private final PeriodId periodId = PeriodId.newId();
    private final EntityId createdByUserId = EntityId.newId();
    private final LedgerAccountId debitAccountId = LedgerAccountId.newId();
    private final LedgerAccountId creditAccountId = LedgerAccountId.newId();

    private JournalEntryLine line(LedgerAccountId accountId, EntryDirection direction, String amount, String label) {
        return JournalEntryLine.of(JournalEntryLineId.newId(), accountId, null, null, direction,
                Amount.of(new BigDecimal(amount)), label);
    }

    private JournalEntry postedEntry() {
        JournalEntry draft = JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "150.00", "Facture entretien"),
                        line(creditAccountId, EntryDirection.CREDIT, "150.00", "Fournisseur")));
        return draft.post(12);
    }

    @Test
    void P10_mirror_lines_flip_direction_and_prefix_the_label() {
        JournalEntry original = postedEntry();

        List<JournalEntryLine> mirrored = original.mirrorLinesForReversal(
                List.of(JournalEntryLineId.newId(), JournalEntryLineId.newId()));

        assertThat(mirrored).hasSize(2);
        assertThat(mirrored.get(0).getDirection()).isEqualTo(EntryDirection.CREDIT);
        assertThat(mirrored.get(0).getLabel()).isEqualTo("Extourne: Facture entretien");
        assertThat(mirrored.get(1).getDirection()).isEqualTo(EntryDirection.DEBIT);
        assertThat(mirrored.get(1).getLabel()).isEqualTo("Extourne: Fournisseur");
    }

    @Test
    void P10_a_mirrored_entry_is_still_balanced_and_linked_to_the_original() {
        JournalEntry original = postedEntry();
        List<JournalEntryLine> mirrored = original.mirrorLinesForReversal(
                List.of(JournalEntryLineId.newId(), JournalEntryLineId.newId()));

        JournalEntry reversal = JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.OD, null, LocalDate.of(2026, 8, 15), null, createdByUserId, mirrored, original.getId())
                .post(13);

        assertThat(reversal.getOriginalEntryId()).contains(original.getId());
        assertThat(reversal.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
    }

    @Test
    void I4_the_original_entry_transitions_to_reversed_with_its_lines_untouched() {
        JournalEntry original = postedEntry();

        JournalEntry reversedOriginal = original.markReversed();

        assertThat(reversedOriginal.getStatus()).isEqualTo(JournalEntryStatus.REVERSED);
        assertThat(reversedOriginal.getLines()).isEqualTo(original.getLines());
    }

    @Test
    void P10_only_a_posted_entry_can_be_reversed() {
        JournalEntry draft = JournalEntry.draft(JournalEntryId.newId(), propertyId, exerciseId, periodId,
                JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, createdByUserId, List.of(
                        line(debitAccountId, EntryDirection.DEBIT, "150.00", "Facture entretien"),
                        line(creditAccountId, EntryDirection.CREDIT, "150.00", "Fournisseur")));

        assertThatThrownBy(draft::markReversed).isInstanceOf(JournalEntryNotPostedException.class);
    }

    @Test
    void mirror_lines_for_reversal_requires_exactly_one_id_per_line() {
        JournalEntry original = postedEntry();

        assertThatThrownBy(() -> original.mirrorLinesForReversal(List.of(JournalEntryLineId.newId())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
