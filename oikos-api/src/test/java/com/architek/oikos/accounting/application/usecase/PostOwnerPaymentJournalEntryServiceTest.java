package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import com.architek.oikos.accounting.application.command.PostOwnerPaymentJournalEntryCommand;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.exception.AccountRoleNotConfiguredException;
import com.architek.oikos.accounting.domain.exception.InvalidTreasuryAccountException;
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
class PostOwnerPaymentJournalEntryServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @Mock
    private PostJournalEntryUseCase postJournalEntryUseCase;

    private PostOwnerPaymentJournalEntryService newService() {
        return new PostOwnerPaymentJournalEntryService(ledgerAccountRepository,
                new TreasuryAccountResolver(ledgerAccountRepository), createJournalEntryDraftUseCase,
                postJournalEntryUseCase);
    }

    private LedgerAccount account(EntityId propertyId, LedgerAccountId id, String number, int accountClass,
                                   AccountNature nature, boolean collective, AccountRole role) {
        return LedgerAccount.create(id, propertyId, null, AccountNumber.of(number), "Label", accountClass, nature,
                collective, role);
    }

    @Test
    void P2_a_payment_fully_imputed_credits_only_the_unit_receivable_account() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        LedgerAccountId receivableAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(Optional.of(
                account(propertyId, cashAccountId, "51610001", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH)));
        when(ledgerAccountRepository.findByPropertyIdAndUnitIdAndRole(propertyId, unitId, AccountRole.UNIT_RECEIVABLE))
                .thenReturn(Optional.of(account(propertyId, receivableAccountId, "34115001", 3, AccountNature.BALANCE_ASSET,
                        false, AccountRole.UNIT_RECEIVABLE)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());

        PostOwnerPaymentJournalEntryCommand command = new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                cashAccountId, LocalDate.of(2026, 1, 15), new BigDecimal("300.00"), BigDecimal.ZERO,
                "Reglement", EntityId.newId());

        newService().post(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        CreateJournalEntryDraftCommand draft = captor.getValue();
        assertThat(draft.journalCode()).isEqualTo(JournalCode.CA);
        assertThat(draft.treasuryAccountId()).isEqualTo(cashAccountId);
        assertThat(draft.lines()).hasSize(2);
        assertThat(draft.lines()).extracting(CreateJournalEntryLineCommand::direction)
                .containsExactlyInAnyOrder(EntryDirection.DEBIT, EntryDirection.CREDIT);
    }

    @Test
    void P3_a_payment_partly_imputed_and_partly_advanced_credits_both_accounts() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccountId bankAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(
                account(propertyId, bankAccountId, "51410001", 5, AccountNature.BALANCE_ASSET, false, AccountRole.BANK)));
        when(ledgerAccountRepository.findByPropertyIdAndUnitIdAndRole(propertyId, unitId, AccountRole.UNIT_RECEIVABLE))
                .thenReturn(Optional.of(account(propertyId, LedgerAccountId.newId(), "34115001", 3, AccountNature.BALANCE_ASSET,
                        false, AccountRole.UNIT_RECEIVABLE)));
        when(ledgerAccountRepository.findGlobalByRole(AccountRole.UNIT_ADVANCE))
                .thenReturn(Optional.of(account(null, LedgerAccountId.newId(), "44150000", 4, AccountNature.BALANCE_LIABILITY,
                        true, AccountRole.UNIT_ADVANCE)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());

        PostOwnerPaymentJournalEntryCommand command = new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                bankAccountId, LocalDate.of(2026, 1, 15), new BigDecimal("300.00"), new BigDecimal("200.00"),
                "Reglement", EntityId.newId());

        newService().post(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        CreateJournalEntryDraftCommand draft = captor.getValue();
        assertThat(draft.journalCode()).isEqualTo(JournalCode.BQ);
        assertThat(draft.lines()).hasSize(3);
        BigDecimal totalCredits = draft.lines().stream().filter(line -> line.direction() == EntryDirection.CREDIT)
                .map(CreateJournalEntryLineCommand::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalCredits).isEqualByComparingTo("500.00");
        assertThat(draft.lines()).allMatch(line -> unitId.equals(line.auxiliaryUnitId()));
    }

    @Test
    void a_payment_fully_advanced_does_not_require_a_unit_receivable_account() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(Optional.of(
                account(propertyId, cashAccountId, "51610001", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH)));
        when(ledgerAccountRepository.findGlobalByRole(AccountRole.UNIT_ADVANCE))
                .thenReturn(Optional.of(account(null, LedgerAccountId.newId(), "44150000", 4, AccountNature.BALANCE_LIABILITY,
                        true, AccountRole.UNIT_ADVANCE)));
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());

        PostOwnerPaymentJournalEntryCommand command = new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                cashAccountId, LocalDate.of(2026, 1, 15), BigDecimal.ZERO, new BigDecimal("300.00"),
                "Reglement", EntityId.newId());

        newService().post(command);

        ArgumentCaptor<CreateJournalEntryDraftCommand> captor = ArgumentCaptor.forClass(CreateJournalEntryDraftCommand.class);
        verify(createJournalEntryDraftUseCase).create(captor.capture());
        assertThat(captor.getValue().lines()).hasSize(2);
    }

    @Test
    void missing_treasury_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(Optional.empty());

        PostOwnerPaymentJournalEntryCommand command = new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                cashAccountId, LocalDate.of(2026, 1, 15), new BigDecimal("300.00"), BigDecimal.ZERO,
                "Reglement", EntityId.newId());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> newService().post(command))
                .isInstanceOf(InvalidTreasuryAccountException.class);
    }

    @Test
    void missing_unit_receivable_account_is_rejected() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        LedgerAccountId cashAccountId = LedgerAccountId.newId();
        when(ledgerAccountRepository.findById(cashAccountId)).thenReturn(Optional.of(
                account(propertyId, cashAccountId, "51610001", 5, AccountNature.BALANCE_ASSET, false, AccountRole.CASH)));
        when(ledgerAccountRepository.findByPropertyIdAndUnitIdAndRole(propertyId, unitId, AccountRole.UNIT_RECEIVABLE))
                .thenReturn(Optional.empty());

        PostOwnerPaymentJournalEntryCommand command = new PostOwnerPaymentJournalEntryCommand(propertyId, unitId,
                cashAccountId, LocalDate.of(2026, 1, 15), new BigDecimal("300.00"), BigDecimal.ZERO,
                "Reglement", EntityId.newId());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> newService().post(command))
                .isInstanceOf(AccountRoleNotConfiguredException.class);
    }
}
