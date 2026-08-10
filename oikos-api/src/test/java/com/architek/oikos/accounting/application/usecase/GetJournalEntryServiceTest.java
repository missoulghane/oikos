package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.query.GetJournalEntryQuery;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
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
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private GetJournalEntryService newService() {
        return new GetJournalEntryService(journalEntryRepository);
    }

    @Test
    void returns_the_entry_with_its_lines() {
        JournalEntryId entryId = JournalEntryId.newId();
        JournalEntryLine line = JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")), "Debit");
        JournalEntry entry = JournalEntry.draft(entryId, EntityId.newId(), AccountingExerciseId.newId(),
                PeriodId.newId(), JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, EntityId.newId(),
                List.of(line, line));
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.of(entry));

        var view = newService().get(new GetJournalEntryQuery(entryId));

        assertThat(view.id()).isEqualTo(entryId);
        assertThat(view.lines()).hasSize(2);
    }

    @Test
    void rejects_an_unknown_entry() {
        JournalEntryId entryId = JournalEntryId.newId();
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().get(new GetJournalEntryQuery(entryId)))
                .isInstanceOf(JournalEntryNotFoundException.class);
    }
}
