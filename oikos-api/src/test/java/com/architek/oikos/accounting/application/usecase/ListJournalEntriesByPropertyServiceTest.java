package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.query.ListJournalEntriesByPropertyQuery;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ListJournalEntriesByPropertyServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ListJournalEntriesByPropertyService newService() {
        return new ListJournalEntriesByPropertyService(journalEntryRepository);
    }

    @Test
    void lists_entries_for_the_property() {
        EntityId propertyId = EntityId.newId();
        JournalEntryLine line = JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")), "Debit");
        JournalEntry entry = JournalEntry.draft(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, EntityId.newId(),
                List.of(line, line));
        PageRequest pageRequest = PageRequest.defaultRequest();
        when(journalEntryRepository.findPageByPropertyId(propertyId, pageRequest))
                .thenReturn(Page.of(List.of(entry), 0, 20, 1));

        var page = newService().list(new ListJournalEntriesByPropertyQuery(propertyId, pageRequest));

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
    }
}
