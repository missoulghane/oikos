package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.ClosePeriodCommand;
import com.architek.oikos.accounting.domain.exception.PeriodNotClosableException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ClosePeriodServiceTest {

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private PeriodRepository periodRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private ClosePeriodService newService() {
        return new ClosePeriodService(enforceExerciseOpenService, periodRepository, journalEntryRepository);
    }

    private List<JournalEntryLine> twoLines() {
        return List.of(
                JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                        EntryDirection.DEBIT, Amount.of(new java.math.BigDecimal("100.00")), "Debit"),
                JournalEntryLine.of(JournalEntryLineId.newId(), LedgerAccountId.newId(), null, null,
                        EntryDirection.CREDIT, Amount.of(new java.math.BigDecimal("100.00")), "Credit"));
    }

    private JournalEntry postedEntry(AccountingExerciseId exerciseId, PeriodId periodId, JournalCode journalCode,
                                      int pieceNumber) {
        JournalEntry draft = JournalEntry.draft(JournalEntryId.newId(), EntityId.newId(), exerciseId, periodId,
                journalCode, null, LocalDate.of(2026, 1, 15), null, EntityId.newId(), twoLines());
        return draft.post(pieceNumber);
    }

    @Test
    void P8_closes_a_clean_period_with_no_violations() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 1));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2026, 1)))
                .thenReturn(Optional.of(period));
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2025, 12)))
                .thenReturn(Optional.empty());
        when(journalEntryRepository.findAllByPeriodId(period.getId())).thenReturn(List.of(
                postedEntry(exercise.getId(), period.getId(), JournalCode.VT, 1)));
        when(periodRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClosePeriodCommand command = new ClosePeriodCommand(propertyId, YearMonth.of(2026, 1), EntityId.newId());

        var result = newService().close(command);

        assertThat(result.status()).isEqualTo(PeriodStatus.CLOSED);
    }

    @Test
    void P8_a_remaining_draft_entry_blocks_closing() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 1));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2026, 1)))
                .thenReturn(Optional.of(period));
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2025, 12)))
                .thenReturn(Optional.empty());
        JournalEntry draftEntry = JournalEntry.draft(JournalEntryId.newId(), propertyId, exercise.getId(), period.getId(),
                JournalCode.OD, null, LocalDate.of(2026, 1, 15), null, EntityId.newId(), twoLines());
        when(journalEntryRepository.findAllByPeriodId(period.getId())).thenReturn(List.of(draftEntry));

        ClosePeriodCommand command = new ClosePeriodCommand(propertyId, YearMonth.of(2026, 1), EntityId.newId());

        assertThatThrownBy(() -> newService().close(command)).isInstanceOf(PeriodNotClosableException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void P8_an_open_previous_period_blocks_closing() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period january = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 1));
        Period february = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 2));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2026, 2)))
                .thenReturn(Optional.of(february));
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2026, 1)))
                .thenReturn(Optional.of(january));
        when(journalEntryRepository.findAllByPeriodId(february.getId())).thenReturn(List.of());

        ClosePeriodCommand command = new ClosePeriodCommand(propertyId, YearMonth.of(2026, 2), EntityId.newId());

        assertThatThrownBy(() -> newService().close(command)).isInstanceOf(PeriodNotClosableException.class)
                .hasMessageContaining("Previous period");
    }

    @Test
    void P8_two_journals_with_their_own_contiguous_numbering_do_not_block_closing() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 1));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2026, 1)))
                .thenReturn(Optional.of(period));
        when(periodRepository.findByExerciseIdAndYearMonth(exercise.getId(), YearMonth.of(2025, 12)))
                .thenReturn(Optional.empty());
        when(journalEntryRepository.findAllByPeriodId(period.getId())).thenReturn(List.of(
                postedEntry(exercise.getId(), period.getId(), JournalCode.VT, 5),
                postedEntry(exercise.getId(), period.getId(), JournalCode.AC, 1)));
        when(periodRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        ClosePeriodCommand command = new ClosePeriodCommand(propertyId, YearMonth.of(2026, 1), EntityId.newId());

        var result = newService().close(command);

        assertThat(result.status()).isEqualTo(PeriodStatus.CLOSED);
    }
}
