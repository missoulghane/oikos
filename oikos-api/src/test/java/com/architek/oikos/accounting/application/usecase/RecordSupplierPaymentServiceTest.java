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
import com.architek.oikos.accounting.application.command.RecordSupplierPaymentCommand;
import com.architek.oikos.accounting.application.dto.ExpenseView;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.InvalidTreasuryAccountException;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.Expense;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.ExpenseRepository;
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
class RecordSupplierPaymentServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    @Mock
    private ExpenseRepository expenseRepository;

    private RecordSupplierPaymentService newService() {
        return new RecordSupplierPaymentService(propertyDirectoryPort, new TreasuryAccountResolver(ledgerAccountRepository),
                createJournalEntryDraftUseCase, postJournalEntryUseCase, expenseRepository);
    }

    private LedgerAccount treasuryAccount(EntityId propertyId, LedgerAccountId id, String number, AccountRole role) {
        return LedgerAccount.create(id, propertyId, null, AccountNumber.of(number), "Label", 5,
                AccountNature.BALANCE_ASSET, false, role);
    }

    @Test
    void records_a_direct_payment_debiting_the_charge_account_and_crediting_the_treasury_account() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(
                Optional.of(treasuryAccount(propertyId, cashAccountId, "51610001", AccountRole.CASH)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        LedgerAccountId chargeAccountId = LedgerAccountId.newId();

        RecordSupplierPaymentCommand command = new RecordSupplierPaymentCommand(propertyId, LocalDate.of(2026, 3, 5),
                chargeAccountId, cashAccountId, new BigDecimal("450.00"), "Reparation ascenseur", "FAC-2026-001",
                EntityId.newId());

        ExpenseView result = newService().record(command);

        assertThat(result.amount()).isEqualByComparingTo("450.00");
        assertThat(result.ledgerAccountId()).isEqualTo(chargeAccountId);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        CreateJournalEntryDraftCommand draft = captor.getValue();
        assertThat(draft.journalCode()).isEqualTo(JournalCode.CA);
        assertThat(draft.treasuryAccountId()).isEqualTo(cashAccountId);
        assertThat(draft.lines()).hasSize(2);
        CreateJournalEntryLineCommand debitLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.DEBIT).findFirst().orElseThrow();
        assertThat(debitLine.ledgerAccountId()).isEqualTo(chargeAccountId);
        assertThat(debitLine.amount()).isEqualByComparingTo("450.00");
        CreateJournalEntryLineCommand creditLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.CREDIT).findFirst().orElseThrow();
        assertThat(creditLine.ledgerAccountId()).isEqualTo(cashAccountId);

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(expenseCaptor.capture());
        assertThat(expenseCaptor.getValue().getDescription()).contains("Reparation ascenseur");
    }

    @Test
    void a_bank_settlement_uses_the_BQ_journal() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId bankAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(bankAccountId)).thenReturn(
                Optional.of(treasuryAccount(propertyId, bankAccountId, "51410001", AccountRole.BANK)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());
        when(expenseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordSupplierPaymentCommand command = new RecordSupplierPaymentCommand(propertyId, LocalDate.of(2026, 3, 5),
                LedgerAccountId.newId(), bankAccountId, new BigDecimal("100.00"), null, null, EntityId.newId());

        newService().record(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        assertThat(captor.getValue().journalCode()).isEqualTo(JournalCode.BQ);
    }

    @Test
    void a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        RecordSupplierPaymentCommand command = new RecordSupplierPaymentCommand(propertyId, LocalDate.of(2026, 3, 5),
                LedgerAccountId.newId(), LedgerAccountId.newId(), new BigDecimal("100.00"), null, null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(PropertyNotFoundException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase, expenseRepository);
    }

    @Test
    void a_missing_treasury_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(Optional.empty());

        RecordSupplierPaymentCommand command = new RecordSupplierPaymentCommand(propertyId, LocalDate.of(2026, 3, 5),
                LedgerAccountId.newId(), cashAccountId, new BigDecimal("100.00"), null, null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(InvalidTreasuryAccountException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase, expenseRepository);
    }

    @Test
    void a_treasury_account_belonging_to_another_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(
                Optional.of(treasuryAccount(EntityId.newId(), cashAccountId, "51610001", AccountRole.CASH)));

        RecordSupplierPaymentCommand command = new RecordSupplierPaymentCommand(propertyId, LocalDate.of(2026, 3, 5),
                LedgerAccountId.newId(), cashAccountId, new BigDecimal("100.00"), null, null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(InvalidTreasuryAccountException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase, expenseRepository);
    }
}
