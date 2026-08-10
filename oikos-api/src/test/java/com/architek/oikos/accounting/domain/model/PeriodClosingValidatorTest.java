package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.model.PeriodClosingValidator.TreasuryReconciliation;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;

class PeriodClosingValidatorTest {

    @Test
    void P8_a_clean_period_has_no_violations() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED, JournalEntryStatus.POSTED),
                Map.of(JournalCode.VT, List.of(1, 2, 3)), null);

        assertThat(violations).isEmpty();
        assertThat(PeriodClosingValidator.isClosable(
                List.of(JournalEntryStatus.POSTED), Map.of(JournalCode.VT, List.of(1, 2)), null)).isTrue();
    }

    @Test
    void P8_a_remaining_draft_entry_is_a_violation() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED, JournalEntryStatus.DRAFT),
                Map.of(JournalCode.VT, List.of(1, 2)), null);

        assertThat(violations).contains("At least one journal entry is still DRAFT");
    }

    @Test
    void P8_a_gap_in_piece_numbering_is_a_violation() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED), Map.of(JournalCode.VT, List.of(1, 2, 4)), null);

        assertThat(violations).contains("Piece numbering has a gap");
    }

    @Test
    void P8_two_journals_each_internally_contiguous_are_not_a_violation() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED),
                Map.of(JournalCode.VT, List.of(5, 6), JournalCode.BQ, List.of(9)), null);

        assertThat(violations).isEmpty();
    }

    @Test
    void P8_a_treasury_reconciliation_mismatch_is_a_violation() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED), Map.of(JournalCode.CA, List.of(1)),
                new TreasuryReconciliation(new BigDecimal("100.00"), new BigDecimal("90.00")));

        assertThat(violations).contains("Theoretical treasury balance does not match the last reconciliation");
    }

    @Test
    void P8_a_matching_treasury_reconciliation_is_not_a_violation() {
        List<String> violations = PeriodClosingValidator.violations(
                List.of(JournalEntryStatus.POSTED), Map.of(JournalCode.CA, List.of(1)),
                new TreasuryReconciliation(new BigDecimal("100.00"), new BigDecimal("100.00")));

        assertThat(violations).isEmpty();
    }
}
