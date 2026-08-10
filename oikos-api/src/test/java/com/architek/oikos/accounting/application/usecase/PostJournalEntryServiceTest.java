package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.PostJournalEntryCommand;
import com.architek.oikos.accounting.domain.exception.JournalEntryNotFoundException;
import com.architek.oikos.accounting.domain.model.JournalEntry;
import com.architek.oikos.accounting.domain.model.JournalEntryLine;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.JournalEntryRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class PostJournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    private PostJournalEntryService newService() {
        return new PostJournalEntryService(journalEntryRepository, ledgerAccountRepository);
    }

    private JournalEntry draftEntry(JournalEntryId id, EntityId propertyId, LedgerAccountId debitAccountId,
                                     LedgerAccountId creditAccountId) {
        JournalEntryLine debit = JournalEntryLine.of(JournalEntryLineId.newId(), debitAccountId, null, null,
                EntryDirection.DEBIT, Amount.of(new BigDecimal("100.00")), "Debit");
        JournalEntryLine credit = JournalEntryLine.of(JournalEntryLineId.newId(), creditAccountId, null, null,
                EntryDirection.CREDIT, Amount.of(new BigDecimal("100.00")), "Credit");
        return JournalEntry.draft(id, propertyId, AccountingExerciseId.newId(), PeriodId.newId(), JournalCode.OD,
                null, LocalDate.of(2026, 8, 1), null, EntityId.newId(), List.of(debit, credit));
    }

    private LedgerAccount account(LedgerAccountId id, AccountNature nature) {
        return LedgerAccount.create(id, null, null, AccountNumber.of("51610001"), "Label", 5, nature, false, null);
    }

    @Test
    void I7_posts_using_the_next_allocated_piece_number() {
        JournalEntryId entryId = JournalEntryId.newId();
        EntityId propertyId = EntityId.newId();
        LedgerAccountId debitAccountId = LedgerAccountId.newId();
        LedgerAccountId creditAccountId = LedgerAccountId.newId();
        JournalEntry draft = draftEntry(entryId, propertyId, debitAccountId, creditAccountId);
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.of(draft));
        when(journalEntryRepository.nextPieceNumber(propertyId, draft.getExerciseId(), JournalCode.OD)).thenReturn(7);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerAccountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(account(debitAccountId, AccountNature.BALANCE_ASSET)));
        when(ledgerAccountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(account(creditAccountId, AccountNature.BALANCE_LIABILITY)));

        var result = newService().post(new PostJournalEntryCommand(entryId));

        assertThat(result.pieceNumber()).isEqualTo(7);
        assertThat(result.status()).isEqualTo(JournalEntryStatus.POSTED);
    }

    @Test
    void posting_credits_the_balance_of_a_debit_normal_account_touched_by_its_own_direction() {
        JournalEntryId entryId = JournalEntryId.newId();
        EntityId propertyId = EntityId.newId();
        LedgerAccountId debitAccountId = LedgerAccountId.newId();
        LedgerAccountId creditAccountId = LedgerAccountId.newId();
        JournalEntry draft = draftEntry(entryId, propertyId, debitAccountId, creditAccountId);
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.of(draft));
        when(journalEntryRepository.nextPieceNumber(any(), any(), any())).thenReturn(1);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // debit-normal account (e.g. a bank/cash account) debited: balance increases.
        when(ledgerAccountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(account(debitAccountId, AccountNature.BALANCE_ASSET)));
        // credit-normal account (e.g. a liability) credited: balance also increases (its own normal side).
        when(ledgerAccountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(account(creditAccountId, AccountNature.BALANCE_LIABILITY)));

        newService().post(new PostJournalEntryCommand(entryId));

        verify(ledgerAccountRepository).incrementBalance(debitAccountId, new BigDecimal("100.00"));
        verify(ledgerAccountRepository).incrementBalance(creditAccountId, new BigDecimal("100.00"));
    }

    @Test
    void posting_decreases_the_balance_when_a_line_moves_an_account_against_its_own_normal_side() {
        JournalEntryId entryId = JournalEntryId.newId();
        EntityId propertyId = EntityId.newId();
        LedgerAccountId debitAccountId = LedgerAccountId.newId();
        LedgerAccountId creditAccountId = LedgerAccountId.newId();
        JournalEntry draft = draftEntry(entryId, propertyId, debitAccountId, creditAccountId);
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.of(draft));
        when(journalEntryRepository.nextPieceNumber(any(), any(), any())).thenReturn(1);
        when(journalEntryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // debit-normal account debited normally: +100.
        when(ledgerAccountRepository.findById(debitAccountId))
                .thenReturn(Optional.of(account(debitAccountId, AccountNature.BALANCE_ASSET)));
        // debit-normal account credited (against its own side, e.g. a cash payment): -100.
        when(ledgerAccountRepository.findById(creditAccountId))
                .thenReturn(Optional.of(account(creditAccountId, AccountNature.BALANCE_ASSET)));

        newService().post(new PostJournalEntryCommand(entryId));

        verify(ledgerAccountRepository).incrementBalance(creditAccountId, new BigDecimal("-100.00"));
    }

    @Test
    void posting_an_unknown_entry_is_rejected() {
        JournalEntryId entryId = JournalEntryId.newId();
        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().post(new PostJournalEntryCommand(entryId)))
                .isInstanceOf(JournalEntryNotFoundException.class);
        verifyNoInteractions(ledgerAccountRepository);
    }
}
