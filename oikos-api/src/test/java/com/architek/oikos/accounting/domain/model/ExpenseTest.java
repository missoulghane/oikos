package com.architek.oikos.accounting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class ExpenseTest {

    @Test
    void P4_create_builds_a_supplier_expense_linked_to_its_journal_entry() {
        EntityId propertyId = EntityId.newId();
        LedgerAccountId ledgerAccountId = LedgerAccountId.newId();
        JournalEntryId journalEntryId = JournalEntryId.newId();

        Expense expense = Expense.create(ExpenseId.newId(), propertyId, LocalDate.of(2026, 8, 12),
                ledgerAccountId, Amount.of(new BigDecimal("150.00")), null, "Facture 1045", journalEntryId);

        assertThat(expense.getPropertyId()).isEqualTo(propertyId);
        assertThat(expense.getLedgerAccountId()).isEqualTo(ledgerAccountId);
        assertThat(expense.getAmount().value()).isEqualByComparingTo("150.00");
        assertThat(expense.getDescription()).isEmpty();
        assertThat(expense.getReceiptReference()).contains("Facture 1045");
        assertThat(expense.getJournalEntryId()).isEqualTo(journalEntryId);
    }
}
