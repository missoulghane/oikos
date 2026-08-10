package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.CreateJournalEntryDraftCommand;
import com.architek.oikos.accounting.application.command.CreateJournalEntryLineCommand;
import com.architek.oikos.accounting.application.command.RecordBankChargeCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.InvalidTreasuryAccountException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class RecordBankChargeServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private RecordBankChargeService newService() {
        return new RecordBankChargeService(propertyDirectoryPort, new TreasuryAccountResolver(ledgerAccountRepository),
                createJournalEntryDraftUseCase, postJournalEntryUseCase);
    }

    @Test
    void P7_posts_a_balanced_BQ_entry_debiting_the_charge_account() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId bankAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(
                LedgerAccount.create(bankAccountId, propertyId, null, AccountNumber.of("51410001"), "Banque", 5,
                        AccountNature.BALANCE_ASSET, false, AccountRole.BANK)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());
        LedgerAccountId chargeAccountId = LedgerAccountId.newId();

        RecordBankChargeCommand command = new RecordBankChargeCommand(propertyId, LocalDate.of(2026, 4, 1),
                chargeAccountId, bankAccountId, new BigDecimal("35.00"), "Frais tenue de compte", EntityId.newId());

        newService().record(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        CreateJournalEntryDraftCommand draft = captor.getValue();
        assertThat(draft.journalCode()).isEqualTo(JournalCode.BQ);
        assertThat(draft.treasuryAccountId()).isEqualTo(bankAccountId);
        assertThat(draft.lines()).hasSize(2);
        CreateJournalEntryLineCommand debitLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.DEBIT).findFirst().orElseThrow();
        assertThat(debitLine.ledgerAccountId()).isEqualTo(chargeAccountId);
        CreateJournalEntryLineCommand creditLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.CREDIT).findFirst().orElseThrow();
        assertThat(creditLine.ledgerAccountId()).isEqualTo(bankAccountId);
    }

    @Test
    void a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        RecordBankChargeCommand command = new RecordBankChargeCommand(propertyId, LocalDate.of(2026, 4, 1),
                LedgerAccountId.newId(), LedgerAccountId.newId(), new BigDecimal("35.00"), null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(PropertyNotFoundException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase);
    }

    @Test
    void a_missing_bank_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId bankAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());

        RecordBankChargeCommand command = new RecordBankChargeCommand(propertyId, LocalDate.of(2026, 4, 1),
                LedgerAccountId.newId(), bankAccountId, new BigDecimal("35.00"), null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(InvalidTreasuryAccountException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase);
    }

    @Test
    void a_bank_account_belonging_to_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId bankAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(
                LedgerAccount.create(bankAccountId, EntityId.newId(), null, AccountNumber.of("51410001"), "Banque", 5,
                        AccountNature.BALANCE_ASSET, false, AccountRole.BANK)));

        RecordBankChargeCommand command = new RecordBankChargeCommand(propertyId, LocalDate.of(2026, 4, 1),
                LedgerAccountId.newId(), bankAccountId, new BigDecimal("35.00"), null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(InvalidTreasuryAccountException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase);
    }
}
