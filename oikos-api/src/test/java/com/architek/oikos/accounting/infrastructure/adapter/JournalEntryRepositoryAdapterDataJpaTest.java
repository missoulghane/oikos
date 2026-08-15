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

import com.architek.oikos.accounting.domain.model.AuxiliaryUnitBalance;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryFilter;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntrySortField;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.infrastructure.mapper.JournalEntryPersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.pagination.SortDirection;
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

    @Test
    void sums_net_amount_for_one_auxiliary_unit_credit_minus_debit_posted_only() {
        EntityId propertyId = EntityId.newId();
        LedgerAccountId accountId = LedgerAccountId.newId();
        EntityId unitId = EntityId.newId();

        JournalEntry credit500 = JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, EntityId.newId(),
                List.of(JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), unitId, null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("500.00")), "Debit"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), accountId, unitId, null,
                                EntryDirection.CREDIT, Amount.of(new BigDecimal("500.00")), "Avance")));
        JournalEntry debit200 = JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 2), null, EntityId.newId(),
                List.of(JournalEntryLine.of(JournalEntryLineId.newId(), accountId, unitId, null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("200.00")), "Regularisation"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), unitId, null,
                                EntryDirection.CREDIT, Amount.of(new BigDecimal("200.00")), "Regularisation")));
        // A DRAFT entry on the same account/unit must not count.
        JournalEntry draftOnly = JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 3), null, EntityId.newId(),
                List.of(JournalEntryLine.of(JournalEntryLineId.newId(), accountId, unitId, null,
                                EntryDirection.CREDIT, Amount.of(new BigDecimal("999.00")), "Ignored"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), unitId, null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("999.00")), "Ignored")));

        adapter.save(credit500.post(1));
        adapter.save(debit200.post(2));
        adapter.save(draftOnly);

        BigDecimal balance = adapter.sumNetAmountForAuxiliaryUnit(propertyId, accountId, unitId);
        assertThat(balance).isEqualByComparingTo("300.00");

        List<AuxiliaryUnitBalance> grouped = adapter.sumNetAmountGroupedByAuxiliaryUnit(propertyId, accountId);
        assertThat(grouped).hasSize(1);
        assertThat(grouped.get(0).unitId()).isEqualTo(unitId);
        assertThat(grouped.get(0).amount()).isEqualByComparingTo("300.00");
    }

    /** Two entries on one treasury account, dated a month apart. */
    private LedgerAccountId seedTwoTreasuryEntries(EntityId propertyId) {
        LedgerAccountId treasuryAccountId = LedgerAccountId.newId();
        adapter.save(treasuryEntry(propertyId, treasuryAccountId, LocalDate.of(2026, 7, 1)));
        adapter.save(treasuryEntry(propertyId, treasuryAccountId, LocalDate.of(2026, 8, 1)));
        return treasuryAccountId;
    }

    private JournalEntry treasuryEntry(EntityId propertyId, LedgerAccountId treasuryAccountId, LocalDate pieceDate) {
        return JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(), PeriodId.newId(),
                JournalCode.OD, null, pieceDate, null, EntityId.newId(),
                List.of(JournalEntryLine.of(JournalEntryLineId.newId(), treasuryAccountId, null, null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("50.00")), "Debit"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                                EntryDirection.CREDIT, Amount.of(new BigDecimal("50.00")), "Credit")));
    }

    @Test
    void treasury_operations_come_back_most_recent_first_when_no_sort_is_asked_for() {
        EntityId propertyId = EntityId.newId();
        LedgerAccountId treasuryAccountId = seedTwoTreasuryEntries(propertyId);

        var page = adapter.findPageByTreasuryAccount(propertyId, treasuryAccountId,
                JournalEntryFilter.defaultFilter(), PageRequest.of(0, 20));

        // The order the JPQL used to carry itself, now set through the Pageable.
        assertThat(page.content()).extracting(JournalEntry::getPieceDate)
                .containsExactly(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 7, 1));
    }

    @Test
    void treasury_operations_can_be_ordered_by_piece_date_ascending() {
        EntityId propertyId = EntityId.newId();
        LedgerAccountId treasuryAccountId = seedTwoTreasuryEntries(propertyId);

        var page = adapter.findPageByTreasuryAccount(propertyId, treasuryAccountId,
                new JournalEntryFilter(null, null, null, null, JournalEntrySortField.PIECE_DATE, SortDirection.ASC),
                PageRequest.of(0, 20));

        assertThat(page.content()).extracting(JournalEntry::getPieceDate)
                .containsExactly(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1));
    }

    @Test
    void treasury_operations_are_ordered_before_being_paginated() {
        EntityId propertyId = EntityId.newId();
        LedgerAccountId treasuryAccountId = seedTwoTreasuryEntries(propertyId);

        // Page 1 of size 1: only a sort applied before paginating puts August here.
        var page = adapter.findPageByTreasuryAccount(propertyId, treasuryAccountId,
                new JournalEntryFilter(null, null, null, null, JournalEntrySortField.PIECE_DATE, SortDirection.ASC),
                PageRequest.of(1, 1));

        assertThat(page.content()).extracting(JournalEntry::getPieceDate).containsExactly(LocalDate.of(2026, 8, 1));
        assertThat(page.totalElements()).isEqualTo(2);
    }
}
