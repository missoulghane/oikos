package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.domain.exception.CollectiveAccountRequiresAuxiliaryException;
import com.architek.oikos.accounting.domain.exception.LedgerAccountNotFoundException;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CreateJournalEntryDraftServiceTest {

    @Mock
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private CreateJournalEntryDraftService newService() {
        return new CreateJournalEntryDraftService(enforceExerciseOpenService, ledgerAccountRepository, journalEntryRepository);
    }

    private LedgerAccount directAccount(LedgerAccountId id) {
        return LedgerAccount.create(id, null, null, AccountNumber.of("61220000"), "Fournitures", 6,
                AccountNature.EXPENSE, false, null);
    }

    @Test
    void I5_creates_a_draft_when_the_piece_date_falls_in_an_open_period() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 8));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(enforceExerciseOpenService.requireOpenPeriod(exercise, LocalDate.of(2026, 8, 1))).thenReturn(period);

        LedgerAccountId debitAccountId = LedgerAccountId.newId();
        LedgerAccountId creditAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(debitAccountId)).thenReturn(Optional.of(directAccount(debitAccountId)));
        when(ledgerAccountRepository.findById(creditAccountId)).thenReturn(Optional.of(directAccount(creditAccountId)));
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreateJournalEntryDraftCommand command = new CreateJournalEntryDraftCommand(propertyId, JournalCode.OD, null,
                LocalDate.of(2026, 8, 1), null, EntityId.newId(), List.of(
                        new CreateJournalEntryLineCommand(debitAccountId, null, null, EntryDirection.DEBIT,
                                new BigDecimal("100.00"), "Debit"),
                        new CreateJournalEntryLineCommand(creditAccountId, null, null, EntryDirection.CREDIT,
                                new BigDecimal("100.00"), "Credit")));

        newService().create(command);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        org.mockito.Mockito.verify(journalEntryRepository).save(captor.capture());
        assertThat(captor.getValue().getLines()).hasSize(2);
    }

    @Test
    void a_line_referencing_an_unknown_ledger_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 8));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(enforceExerciseOpenService.requireOpenPeriod(exercise, LocalDate.of(2026, 8, 1))).thenReturn(period);

        LedgerAccountId unknownId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(unknownId)).thenReturn(Optional.empty());

        CreateJournalEntryDraftCommand command = new CreateJournalEntryDraftCommand(propertyId, JournalCode.OD, null,
                LocalDate.of(2026, 8, 1), null, EntityId.newId(), List.of(
                        new CreateJournalEntryLineCommand(unknownId, null, null, EntryDirection.DEBIT,
                                new BigDecimal("100.00"), "Debit"),
                        new CreateJournalEntryLineCommand(LedgerAccountId.newId(), null, null, EntryDirection.CREDIT,
                                new BigDecimal("100.00"), "Credit")));

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(LedgerAccountNotFoundException.class);
    }

    @Test
    void I6_a_line_on_a_collective_account_without_an_auxiliary_is_rejected() {
        EntityId propertyId = EntityId.newId();
        AccountingExercise exercise = AccountingExercise.open(AccountingExerciseId.newId(), propertyId, "Exercice 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        Period period = Period.open(PeriodId.newId(), exercise.getId(), YearMonth.of(2026, 8));
        when(enforceExerciseOpenService.requireOpenExercise(propertyId)).thenReturn(exercise);
        when(enforceExerciseOpenService.requireOpenPeriod(exercise, LocalDate.of(2026, 8, 1))).thenReturn(period);

        LedgerAccountId collectiveId = LedgerAccountId.newId();
        LedgerAccount collective = LedgerAccount.create(collectiveId, null, null, AccountNumber.of("44150000"),
                "Avances", 4, AccountNature.BALANCE_LIABILITY, true, AccountRole.UNIT_ADVANCE);
        when(ledgerAccountRepository.findById(collectiveId)).thenReturn(Optional.of(collective));

        CreateJournalEntryDraftCommand command = new CreateJournalEntryDraftCommand(propertyId, JournalCode.OD, null,
                LocalDate.of(2026, 8, 1), null, EntityId.newId(), List.of(
                        new CreateJournalEntryLineCommand(collectiveId, null, null, EntryDirection.CREDIT,
                                new BigDecimal("100.00"), "Avance"),
                        new CreateJournalEntryLineCommand(LedgerAccountId.newId(), null, null, EntryDirection.DEBIT,
                                new BigDecimal("100.00"), "Contrepartie")));

        assertThatThrownBy(() -> newService().create(command))
                .isInstanceOf(CollectiveAccountRequiresAuxiliaryException.class);
    }
}
