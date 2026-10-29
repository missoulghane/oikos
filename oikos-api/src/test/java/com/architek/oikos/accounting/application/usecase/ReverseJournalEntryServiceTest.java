package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.application.command.ReverseJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotPostedException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ReverseJournalEntryServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId USER_ID = EntityId.newId();
    private static final LocalDate ORIGINAL_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate TODAY = LocalDate.of(2027, 2, 10);

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private ReverseJournalEntryService newService() {
        return new ReverseJournalEntryService(journalEntryRepository, accountingExerciseRepository,
                enforceExerciseOpenService, postJournalEntryUseCase);
    }

    private static AccountingExercise exercise(int year) {
        return AccountingExercise.open(AccountingExerciseId.newId(), PROPERTY_ID, "Exercice " + year,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), null);
    }

    private static JournalEntry postedFundCall(AccountingExerciseId exerciseId) {
        LedgerAccountId receivable = LedgerAccountId.newId();
        LedgerAccountId income = LedgerAccountId.newId();
        return JournalEntry.draft(JournalEntryId.newId(), PROPERTY_ID, exerciseId, JournalCode.VT, null, ORIGINAL_DATE,
                "Appel de fonds 2026-03", USER_ID, List.of(
                        JournalEntryLine.of(JournalEntryLineId.newId(), receivable, EntityId.newId(), null,
                                EntryDirection.DEBIT, Amount.of(new BigDecimal("300")), "Appel de fonds"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), income, null, null, EntryDirection.CREDIT,
                                Amount.of(new BigDecimal("300")), "Cotisations"))).post(4);
    }

    private ReverseJournalEntryCommand command(JournalEntry original) {
        return new ReverseJournalEntryCommand(PROPERTY_ID, original.getId(), TODAY, USER_ID);
    }

    @Test
    void P10_the_mirror_is_dated_like_the_original_while_its_exercise_is_open() {
        AccountingExercise exercise2026 = exercise(2026);
        JournalEntry original = postedFundCall(exercise2026.getId());
        when(journalEntryRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(accountingExerciseRepository.findById(exercise2026.getId())).thenReturn(Optional.of(exercise2026));
        when(enforceExerciseOpenService.requireOpenExerciseCovering(PROPERTY_ID, ORIGINAL_DATE)).thenReturn(exercise2026);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntryId mirrorId = newService().reverse(command(original));

        ArgumentCaptor<JournalEntry> saved = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        JournalEntry mirror = saved.getAllValues().get(0);
        assertThat(mirror.getId()).isEqualTo(mirrorId);
        assertThat(mirror.getPieceDate()).isEqualTo(ORIGINAL_DATE);
        assertThat(mirror.getExerciseId()).isEqualTo(exercise2026.getId());
        assertThat(mirror.getOriginalEntryId()).contains(original.getId());
        assertThat(mirror.getJournalCode()).isEqualTo(JournalCode.VT);
        assertThat(mirror.getLines()).extracting(JournalEntryLine::getDirection)
                .containsExactly(EntryDirection.CREDIT, EntryDirection.DEBIT);
        assertThat(saved.getAllValues().get(1).getStatus()).isEqualTo(JournalEntryStatus.REVERSED);
        verify(postJournalEntryUseCase).post(new PostJournalEntryCommand(mirrorId));
    }

    @Test
    void P10_the_mirror_lands_in_the_open_exercise_covering_the_reversal_date_once_the_original_one_is_closed() {
        AccountingExercise closed2026 = exercise(2026).close(Instant.parse("2027-01-31T10:00:00Z"), USER_ID);
        AccountingExercise exercise2027 = exercise(2027);
        JournalEntry original = postedFundCall(closed2026.getId());
        when(journalEntryRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(accountingExerciseRepository.findById(closed2026.getId())).thenReturn(Optional.of(closed2026));
        when(enforceExerciseOpenService.requireOpenExerciseCovering(PROPERTY_ID, TODAY)).thenReturn(exercise2027);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().reverse(command(original));

        ArgumentCaptor<JournalEntry> saved = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository, org.mockito.Mockito.times(2)).save(saved.capture());
        JournalEntry mirror = saved.getAllValues().get(0);
        assertThat(mirror.getPieceDate()).isEqualTo(TODAY);
        assertThat(mirror.getExerciseId()).isEqualTo(exercise2027.getId());
    }

    @Test
    void P10_only_a_posted_entry_can_be_reversed() {
        AccountingExercise exercise2026 = exercise(2026);
        JournalEntry reversed = postedFundCall(exercise2026.getId()).markReversed();
        when(journalEntryRepository.findById(reversed.getId())).thenReturn(Optional.of(reversed));

        assertThatThrownBy(() -> newService().reverse(command(reversed)))
                .isInstanceOf(JournalEntryNotPostedException.class);
        verify(journalEntryRepository, never()).save(any());
    }

    @Test
    void an_entry_of_another_property_is_not_found() {
        JournalEntry original = postedFundCall(AccountingExerciseId.newId());
        when(journalEntryRepository.findById(original.getId())).thenReturn(Optional.of(original));

        assertThatThrownBy(() -> newService().reverse(new ReverseJournalEntryCommand(EntityId.newId(),
                original.getId(), TODAY, USER_ID))).isInstanceOf(JournalEntryNotFoundException.class);
    }
}
