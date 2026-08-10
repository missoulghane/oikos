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

import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.infrastructure.mapper.JournalEntryPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.infrastructure.configuration.JpaAuditingConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JournalEntryRepositoryAdapter.class, JournalEntryPersistenceMapperImpl.class, JpaAuditingConfiguration.class})
class JournalEntryRepositoryAdapterDataJpaTest {

    @Autowired
    private JournalEntryRepositoryAdapter adapter;

    private JournalEntry newDraft(EntityId propertyId) {
        JournalEntryLine debit = JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")), "Debit");
        JournalEntryLine credit = JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                EntryDirection.CREDIT, Amount.of(new BigDecimal("100.00")), "Credit");
        return JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(), PeriodId.newId(),
                JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, EntityId.newId(), List.of(debit, credit));
    }

    @Test
    void saves_and_reloads_a_draft_entry_with_its_lines() {
        EntityId propertyId = EntityId.newId();
        JournalEntry saved = adapter.save(newDraft(propertyId));

        JournalEntry reloaded = adapter.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getPropertyId()).isEqualTo(propertyId);
        assertThat(reloaded.getStatus()).isEqualTo(JournalEntryStatus.DRAFT);
        assertThat(reloaded.getLines()).hasSize(2);
        assertThat(reloaded.getLines()).extracting(JournalEntryLine::getLabel).containsExactly("Debit", "Credit");
    }

    @Test
    void posting_updates_status_and_piece_number_without_touching_the_lines() {
        JournalEntry saved = adapter.save(newDraft(EntityId.newId()));

        JournalEntry posted = adapter.save(saved.post(1));

        JournalEntry reloaded = adapter.findById(posted.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(JournalEntryStatus.POSTED);
        assertThat(reloaded.getPieceNumber()).contains(1);
        assertThat(reloaded.getLines()).hasSize(2);
    }

    @Test
    void I7_piece_numbers_are_allocated_sequentially_per_property_exercise_and_journal() {
        EntityId propertyId = EntityId.newId();
        AccountingExerciseId exerciseId = AccountingExerciseId.newId();

        assertThat(adapter.nextPieceNumber(propertyId, exerciseId, JournalCode.OD)).isEqualTo(1);
        assertThat(adapter.nextPieceNumber(propertyId, exerciseId, JournalCode.OD)).isEqualTo(2);
        assertThat(adapter.nextPieceNumber(propertyId, exerciseId, JournalCode.VT)).isEqualTo(1);
    }

    @Test
    void lists_entries_for_a_property_most_recent_first() {
        EntityId propertyId = EntityId.newId();
        JournalEntry older = JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, EntityId.newId(),
                List.of(JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("50.00")), "Debit"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                                EntryDirection.CREDIT, Amount.of(new BigDecimal("50.00")), "Credit")));
        JournalEntry newer = newDraft(propertyId);
        adapter.save(older);
        adapter.save(newer);
        adapter.save(newDraft(EntityId.newId()));

        var page = adapter.findPageByPropertyId(propertyId, PageRequest.defaultRequest());

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.content()).extracting(JournalEntry::getPieceDate)
                .containsExactly(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1));
    }
}
