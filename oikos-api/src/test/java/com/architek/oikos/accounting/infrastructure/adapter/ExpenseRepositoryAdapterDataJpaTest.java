package com.architek.oikos.accounting.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ExpenseRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.ExpensePersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class ExpenseRepositoryAdapterDataJpaTest {

    @Autowired
    private ExpenseRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_an_expense() {
        FinancialAccountId accountId = FinancialAccountId.newId();
        Expense saved = adapter.save(Expense.create(ExpenseId.newId(), AccountingExerciseId.newId(), accountId,
                LocalDate.of(2026, 3, 1), "Gardiennage", "Securitas", Amount.of(new BigDecimal("300")),
                "Mois de mars", null, FinancialJournalEntryId.newId()));

        Expense reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getCategory()).isEqualTo("Gardiennage");
        assertThat(reloaded.getProvider()).isEqualTo("Securitas");
        assertThat(reloaded.getAmount().value()).isEqualByComparingTo("300");
    }

    @Test
    void lists_expenses_of_a_financial_account() {
        FinancialAccountId accountId = FinancialAccountId.newId();
        adapter.save(Expense.create(ExpenseId.newId(), AccountingExerciseId.newId(), accountId, LocalDate.now(),
                "Gardiennage", "Securitas", Amount.of(new BigDecimal("300")), null, null, FinancialJournalEntryId.newId()));
        adapter.save(Expense.create(ExpenseId.newId(), AccountingExerciseId.newId(), accountId, LocalDate.now(),
                "Eau", "Lydec", Amount.of(new BigDecimal("80")), null, null, FinancialJournalEntryId.newId()));

        var page = adapter.findPageByFinancialAccountIds(List.of(accountId), PageRequest.of(0, 20));

        assertThat(page.content()).hasSize(2);
    }
}
