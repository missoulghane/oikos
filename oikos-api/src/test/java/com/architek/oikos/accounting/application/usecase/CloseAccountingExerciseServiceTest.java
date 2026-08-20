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
import java.time.YearMonth;
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
import com.architek.oikos.accounting.domain.exception.ExerciseNotClosableException;
import com.architek.oikos.accounting.domain.model.AccountNetAmount;
import com.architek.oikos.accounting.domain.model.AccountingExercise;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.model.Period;
import com.architek.oikos.accounting.domain.repository.AccountingExerciseRepository;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.repository.PeriodRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.accounting.domain.valueobject.PeriodStatus;
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
    private EnforceExerciseOpenService enforceExerciseOpenService;

    @Mock
    private AccountingExerciseRepository accountingExerciseRepository;

    @Mock
    private PeriodRepository periodRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private ResultAccountResolver resultAccountResolver;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private CloseAccountingExerciseService newService() {
        return new CloseAccountingExerciseService(enforceExerciseOpenService, accountingExerciseRepository,
                periodRepository, journalEntryRepository, ledgerAccountRepository, resultAccountResolver,
                postJournalEntryUseCase);
    }

    private static AccountingExercise openExercise() {
        return AccountingExercise.open(EXERCISE_ID, PROPERTY_ID, "2026", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), null);
    }

    private static Period closedPeriod(YearMonth month) {
        return Period.reconstruct(PeriodId.newId(), EXERCISE_ID, month, PeriodStatus.CLOSED, Instant.now(),
                EntityId.newId(), null, null);
    }

    private static LedgerAccount account(LedgerAccountId id, String number, String label, int accountClass,
                                          AccountNature nature) {
        return LedgerAccount.reconstruct(id, PROPERTY_ID, null, AccountNumber.of(number), label, accountClass, nature,
                false, null, true, BigDecimal.ZERO, null);
    }

    /** Les douze mois clos, ce qui est la condition d'entrée de la clôture. */
    private void givenAllPeriodsClosed() {
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(
                closedPeriod(YearMonth.of(2026, 1)), closedPeriod(YearMonth.of(2026, 12))));
    }

    @Test
    void refuses_while_a_month_is_still_open() {
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        when(periodRepository.findAllByExerciseId(EXERCISE_ID)).thenReturn(List.of(
                closedPeriod(YearMonth.of(2026, 1)), Period.open(PeriodId.newId(), EXERCISE_ID, YearMonth.of(2026, 11))));

        assertThatThrownBy(() -> newService().close(new CloseAccountingExerciseCommand(PROPERTY_ID, USER_ID)))
                .isInstanceOf(ExerciseNotClosableException.class)
                .hasMessageContaining("2026-11");
        verify(accountingExerciseRepository, never()).save(any());
    }

    @Test
    void books_the_result_from_the_property_s_own_entries() {
        // Le point qui compte : les produits d'appels de fonds sont un compte
        // partagé entre toutes les copropriétés. Le solde porté par le compte
        // totalise tout le monde ; seules les écritures de cette copropriété-ci
        // disent ce qu'elle a produit.
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        givenAllPeriodsClosed();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of(
                new AccountNetAmount(DUES_INCOME, new BigDecimal("3000")),
                new AccountNetAmount(MAINTENANCE_EXPENSE, new BigDecimal("-2710")),
                new AccountNetAmount(BANK, new BigDecimal("-290"))));
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of(
                account(DUES_INCOME, "70100000", "Appels de fonds", 7, AccountNature.INCOME),
                account(MAINTENANCE_EXPENSE, "61500000", "Entretien", 6, AccountNature.EXPENSE),
                account(BANK, "51400000", "Banque", 5, AccountNature.BALANCE_ASSET)));
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        // L'écriture de clôture est rattachée au dernier mois de l'exercice.
        when(periodRepository.findByExerciseIdAndYearMonth(EXERCISE_ID, YearMonth.of(2026, 12)))
                .thenReturn(Optional.of(closedPeriod(YearMonth.of(2026, 12))));
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(new CloseAccountingExerciseCommand(PROPERTY_ID, USER_ID));

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
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        givenAllPeriodsClosed();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of());
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of());
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(new CloseAccountingExerciseCommand(PROPERTY_ID, USER_ID));

        assertThat(view.exercise().status()).isEqualTo(ExerciseStatus.CLOSED);
        assertThat(view.exercise().closedByUserId()).isEqualTo(USER_ID);
    }

    @Test
    void an_exercise_without_a_single_movement_closes_without_an_entry() {
        // Une copropriété qui vient d'ouvrir : rien à solder, mais l'exercice
        // doit pouvoir se fermer quand même.
        when(enforceExerciseOpenService.requireOpenExercise(PROPERTY_ID)).thenReturn(openExercise());
        givenAllPeriodsClosed();
        when(journalEntryRepository.sumNetAmountByAccountForExercise(PROPERTY_ID, EXERCISE_ID)).thenReturn(List.of());
        when(ledgerAccountRepository.findAllVisibleToProperty(PROPERTY_ID)).thenReturn(List.of());
        when(resultAccountResolver.resolve(PROPERTY_ID)).thenReturn(RESULT);
        when(accountingExerciseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ExerciseClosingView view = newService().close(new CloseAccountingExerciseCommand(PROPERTY_ID, USER_ID));

        assertThat(view.closingEntryId()).isNull();
        assertThat(view.netResult()).isEqualByComparingTo("0");
        verify(journalEntryRepository, never()).save(any());
        verify(postJournalEntryUseCase, never()).post(any());
    }
}
