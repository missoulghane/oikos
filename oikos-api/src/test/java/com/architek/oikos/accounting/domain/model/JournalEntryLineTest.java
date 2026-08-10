package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class JournalEntryLineTest {

    @Test
    void a_line_cannot_carry_both_a_unit_and_a_party_auxiliary() {
        assertThatThrownBy(() -> JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(),
                EntityId.newId(), EntityId.newId(), EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")),
                "Test line"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void I3_amount_must_be_strictly_positive() {
        assertThatThrownBy(() -> Amount.of(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Amount.of(new BigDecimal("-1.00"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void a_line_without_any_auxiliary_reports_none() {
        JournalEntryLine line = JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")), "Test line");

        assertThat(line.hasAuxiliary()).isFalse();
    }
}
