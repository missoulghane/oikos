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

import com.architek.oikos.accounting.application.command.CloseAccountingExerciseCommand;
import com.architek.oikos.accounting.application.dto.ExerciseClosingView;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.AccountingExerciseClosedException;
import com.architek.oikos.accounting.domain.exception.AccountingExerciseNotFoundException;
import com.architek.oikos.accounting.domain.exception.ExerciseNotClosableException;
import com.architek.oikos.accounting.domain.exception.ExerciseNotOldestOpenException;
import com.architek.oikos.accounting.domain.model.AccountNetAmount;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class CloseAccountingExerciseServiceTest {

    private static final EntityId PROPERTY_ID = EntityId.newId();
    private static final EntityId USER_ID = EntityId.newId();
    private static final AccountingExerciseId EXERCISE_ID = AccountingExerciseId.newId();
    private static final LedgerAccountId DUES_INCOME = LedgerAccountId.newId();
    private static final LedgerAccountId MAINTENANCE_EXPENSE = LedgerAccountId.newId();
    private static final LedgerAccountId BANK = LedgerAccountId.newId();
    private static final LedgerAccountId RESULT = LedgerAccountId.newId();

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private ResultAccountResolver resultAccountResolver;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private CloseAccountingExerciseService newService() {
        return new CloseAccountingExerciseService(accountingExerciseRepository,
                journalEntryRepository, ledgerAccountRepository, resultAccountResolver,
                postJournalEntryUseCase);
    }

    private static AccountingExercise openExercise() {
        return AccountingExercise.open(EXERCISE_ID, PROPERTY_ID, "2026", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null);
    }

    private static CloseAccountingExerciseCommand command() {
        return new CloseAccountingExerciseCommand(PROPERTY_ID, EXERCISE_ID, USER_ID);
    }

    /** L'exercice visé, ouvert et seul ouvert : la clôture peut porter sur lui. */
    private void givenOpenExercise() {
        AccountingExercise exercise = openExercise();
        when(accountingExerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exercise));
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(exercise));
    }

    /** A two-line OD entry, posted under the given piece number, or left DRAFT when it is null. */
    private static JournalEntry entry(Integer pieceNumber) {
        JournalEntry draft = JournalEntry.draft(JournalEntryId.newId(), PROPERTY_ID, EXERCISE_ID, JournalCode.OD, null,
                LocalDate.of(2026, 6, 1), null, USER_ID, List.of(
                        JournalEntryLine.of(JournalEntryLineId.newId(), BANK, null, null, EntryDirection.DEBIT,
                                Amount.of(new BigDecimal("100")), "Debit"),
                        JournalEntryLine.of(JournalEntryLineId.newId(), DUES_INCOME, null, null, EntryDirection.CREDIT,
                                Amount.of(new BigDecimal("100")), "Credit")));
        return pieceNumber == null ? draft : draft.post(pieceNumber);
    }

    private static LedgerAccount account(LedgerAccountId id, String number, String label, int accountClass,
                                          AccountNature nature) {
        return LedgerAccount.reconstruct(id, PROPERTY_ID, null, AccountNumber.of(number), label, accountClass, nature,
                false, null, true, BigDecimal.ZERO, null);
    }

    /** Des écritures toutes comptabilisées et numérotées sans trou : la condition d'entrée de la clôture. */
    private void givenEntriesReadyForClosing() {
        when(journalEntryRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(entry(1), entry(2)));
    }

    @Test
    void refuses_while_an_entry_is_still_draft() {
        givenOpenExercise();
        when(journalEntryRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(entry(1), entry(null)));

        assertThatThrownBy(() -> newService().close(command()))
                .isInstanceOf(ExerciseNotClosableException.class)
                .hasMessageContaining("DRAFT");
        verify(accountingExerciseRepository, never()).save(any());
    }

    @Test
    void refuses_when_piece_numbering_has_a_gap() {
        givenOpenExercise();
        when(journalEntryRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(entry(1), entry(3)));

        assertThatThrownBy(() -> newService().close(command()))
                .isInstanceOf(ExerciseNotClosableException.class)
                .hasMessageContaining("gap");
        verify(accountingExerciseRepository, never()).save(any());
    }

    @Test
    void books_the_result_from_the_property_s_own_entries() {
        // Le point qui compte : les produits d'appels de fonds sont un compte
        // partagé entre toutes les copropriétés. Le solde porté par le compte
        // totalise tout le monde ; seules les écritures de cette copropriété-ci
        // disent ce qu'elle a produit.
        givenOpenExercise();
        givenEntriesReadyForClosing();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of(
                new AccountNetAmount(DUES_INCOME, new BigDecimal("3000")),
                new AccountNetAmount(MAINTENANCE_EXPENSE, new BigDecimal("-2710")),
                new AccountNetAmount(BANK, new BigDecimal("-290"))));
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of(
                account(DUES_INCOME, "70100000", "Appels de fonds", 7, AccountNature.INCOME),
                account(MAINTENANCE_EXPENSE, "61500000", "Entretien", 6, AccountNature.EXPENSE),
                account(BANK, "51400000", "Banque", 5, AccountNature.BALANCE_ASSET)));
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(command());

        assertThat(view.netResult()).isEqualByComparingTo("290");
        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalEntryRepository).save(captor.capture());
        JournalEntry closingEntry = captor.getValue();
        // La banque est un compte de bilan : elle n'a rien à faire dans l'écriture
        // de clôture, qui ne solde que le compte de résultat.
        assertThat(closingEntry.getLines()).hasSize(3);
        assertThat(closingEntry.getLines()).noneMatch(line -> line.getLedgerAccountId().equals(BANK));
        assertThat(closingEntry.getJournalCode()).isEqualTo(JournalCode.OD);
        assertThat(closingEntry.getPieceDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        verify(postJournalEntryUseCase).post(any());
    }

    @Test
    void seals_the_exercise() {
        givenOpenExercise();
        givenEntriesReadyForClosing();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of());
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of());
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(command());

        assertThat(view.exercise().status()).isEqualTo(ExerciseStatus.CLOSED);
        assertThat(view.exercise().closedByUserId()).isEqualTo(USER_ID);
    }

    @Test
    void an_exercise_without_a_single_movement_closes_without_an_entry() {
        // Une copropriété qui vient d'ouvrir : rien à solder, mais l'exercice
        // doit pouvoir se fermer quand même.
        givenOpenExercise();
        givenEntriesReadyForClosing();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of());
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of());
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(command());

        assertThat(view.closingEntryId()).isNull();
        assertThat(view.netResult()).isEqualByComparingTo("0");
        verify(journalEntryRepository, never()).save(any());
        verify(postJournalEntryUseCase, never()).post(any());
    }

    @Test
    void refuses_an_exercise_of_another_property() {
        AccountingExercise foreign = AccountingExercise.open(EXERCISE_ID, EntityId.newId(), "2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);
        when(accountingExerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> newService().close(command()))
                .isInstanceOf(AccountingExerciseNotFoundException.class);
    }

    @Test
    void refuses_an_exercise_already_closed() {
        AccountingExercise closed = openExercise().close(Instant.parse("2027-03-01T10:00:00Z"), USER_ID);
        when(accountingExerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> newService().close(command()))
                .isInstanceOf(AccountingExerciseClosedException.class);
    }

    @Test
    void refuses_the_newer_of_two_open_exercises() {
        // N+1 ne se clôture pas avant N : l'ordre des exercices est celui des comptes approuvés.
        AccountingExercise older = AccountingExercise.open(AccountingExerciseId.newId(), PROPERTY_ID, "2025",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null);
        AccountingExercise target = openExercise();
        when(accountingExerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(target));
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(older, target));

        assertThatThrownBy(() -> newService().close(command()))
                .isInstanceOf(ExerciseNotOldestOpenException.class);
        verify(journalEntryRepository, never()).findAllByExerciseId(any());
    }

    @Test
    void closes_the_older_of_two_open_exercises() {
        AccountingExercise newer = AccountingExercise.open(AccountingExerciseId.newId(), PROPERTY_ID, "2027",
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), null);
        AccountingExercise target = openExercise();
        when(accountingExerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(target));
        when(accountingExerciseRepository.findAllByPropertyId(PROPERTY_ID)).thenReturn(List.of(target, newer));
        givenEntriesReadyForClosing();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of());
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of());
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(command());

        assertThat(view.exercise().status()).isEqualTo(ExerciseStatus.CLOSED);
    }
}
