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

import com.architek.oikos.accounting.domain.model.FinancialJournalEntry;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryDirection;
import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.FinancialJournalEntryId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FinancialJournalEntryRepositoryAdapter.class, com.architek.oikos.accounting.infrastructure.mapper.FinancialJournalEntryPersistenceMapperImpl.class,
        JpaAuditingConfiguration.class})
class FinancialJournalEntryRepositoryAdapterDataJpaTest {

    @Autowired
    private FinancialJournalEntryRepositoryAdapter adapter;

    @Test
    void saves_and_reloads_an_entry() {
        FinancialAccountId accountId = FinancialAccountId.newId();
        FinancialJournalEntry saved = adapter.save(FinancialJournalEntry.create(FinancialJournalEntryId.newId(),
                AccountingExerciseId.newId(), accountId, LocalDate.of(2026, 3, 1), FinancialEntryType.OWNER_PAYMENT,
                FinancialEntryDirection.IN, Amount.of(new BigDecimal("150")), "Paiement", null, EntityId.newId()));

        FinancialJournalEntry reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getFinancialAccountId()).isEqualTo(accountId);
        assertThat(reloaded.getAmount().value()).isEqualByComparingTo("150");
    }

    @Test
    void filters_entries_by_account_and_type() {
        FinancialAccountId accountId = FinancialAccountId.newId();
        adapter.save(FinancialJournalEntry.create(FinancialJournalEntryId.newId(), AccountingExerciseId.newId(),
                accountId, LocalDate.of(2026, 3, 1), FinancialEntryType.OWNER_PAYMENT, FinancialEntryDirection.IN,
                Amount.of(new BigDecimal("150")), "Paiement", null, EntityId.newId()));
        adapter.save(FinancialJournalEntry.create(FinancialJournalEntryId.newId(), AccountingExerciseId.newId(),
                accountId, LocalDate.of(2026, 3, 5), FinancialEntryType.OTHER_EXPENSE, FinancialEntryDirection.OUT,
                Amount.of(new BigDecimal("80")), "Depense", null, EntityId.newId()));

        FinancialJournalEntryFilter filter = new FinancialJournalEntryFilter(null, null,
                FinancialEntryType.OWNER_PAYMENT, null, null);

        var page = adapter.findPageByFinancialAccountIds(List.of(accountId), filter, PageRequest.of(0, 20));

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).getType()).isEqualTo(FinancialEntryType.OWNER_PAYMENT);
    }
}
