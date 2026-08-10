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
import com.architek.oikos.accounting.application.command.RecordPayrollExpenseCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
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
class RecordPayrollExpenseServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private RecordPayrollExpenseService newService() {
        return new RecordPayrollExpenseService(propertyDirectoryPort, ledgerAccountRepository,
                createJournalEntryDraftUseCase, postJournalEntryUseCase);
    }

    @Test
    void P6_posts_a_balanced_OD_entry_crediting_the_staff_payable_account() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        LedgerAccountId staffPayableId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findGlobalByRole(AccountRole.STAFF_PAYABLE)).thenReturn(Optional.of(
                LedgerAccount.create(staffPayableId, null, null, AccountNumber.of("44320000"), "Personnel", 4,
                        AccountNature.BALANCE_LIABILITY, false, AccountRole.STAFF_PAYABLE)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());
        LedgerAccountId salaryAccountId = LedgerAccountId.newId();

        RecordPayrollExpenseCommand command = new RecordPayrollExpenseCommand(propertyId, LocalDate.of(2026, 4, 30),
                salaryAccountId, new BigDecimal("3000.00"), "Salaires avril", EntityId.newId());

        newService().record(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        CreateJournalEntryDraftCommand draft = captor.getValue();
        assertThat(draft.journalCode()).isEqualTo(JournalCode.OD);
        assertThat(draft.treasuryAccountId()).isNull();
        assertThat(draft.lines()).hasSize(2);
        CreateJournalEntryLineCommand debitLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.DEBIT).findFirst().orElseThrow();
        assertThat(debitLine.ledgerAccountId()).isEqualTo(salaryAccountId);
        CreateJournalEntryLineCommand creditLine = draft.lines().stream()
                .filter(line -> line.direction() == EntryDirection.CREDIT).findFirst().orElseThrow();
        assertThat(creditLine.ledgerAccountId()).isEqualTo(staffPayableId);
    }

    @Test
    void a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        RecordPayrollExpenseCommand command = new RecordPayrollExpenseCommand(propertyId, LocalDate.of(2026, 4, 30),
                LedgerAccountId.newId(), new BigDecimal("3000.00"), null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(PropertyNotFoundException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase);
    }

    @Test
    void a_missing_staff_payable_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(ledgerAccountRepository.findGlobalByRole(AccountRole.STAFF_PAYABLE)).thenReturn(Optional.empty());

        RecordPayrollExpenseCommand command = new RecordPayrollExpenseCommand(propertyId, LocalDate.of(2026, 4, 30),
                LedgerAccountId.newId(), new BigDecimal("3000.00"), null, EntityId.newId());

        assertThatThrownBy(() -> newService().record(command)).isInstanceOf(AccountRoleNotConfiguredException.class);
        verifyNoInteractions(createJournalEntryDraftUseCase);
    }
}
