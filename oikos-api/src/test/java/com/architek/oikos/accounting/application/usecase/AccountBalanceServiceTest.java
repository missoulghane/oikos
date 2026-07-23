package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.port.out.UnitDirectoryPort;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.repository.MovementRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * RG010bis: property bookkeeping mirrors unit bookkeeping (opposite
 * direction, every movement - payments included). RG010 (revised): balance is
 * persisted, updated incrementally here rather than recomputed.
 */
@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private UnitDirectoryPort unitDirectoryPort;

    private AccountBalanceService newService() {
        return new AccountBalanceService(accountRepository, movementRepository, unitDirectoryPort);
    }

    @Test
    void a_debit_on_a_unit_account_is_mirrored_as_a_credit_on_its_property_account() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        Account unitAccount = Account.create(AccountId.newId(), unitId, AccountType.UNIT);
        Account propertyAccount = Account.create(AccountId.newId(), propertyId, AccountType.PROPERTY);
        Movement debit = Movement.create(MovementId.newId(), unitAccount.getId(), Instant.parse("2027-01-01T00:00:00Z"),
                MovementType.INSTALLMENT, MovementDirection.DEBIT, Amount.of(new BigDecimal("250")), "Appel", null);

        when(unitDirectoryPort.resolvePropertyId(unitId)).thenReturn(propertyId);
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.of(propertyAccount));

        newService().postMovement(unitAccount, debit);

        ArgumentCaptor<Account> savedAccounts = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, org.mockito.Mockito.times(2)).save(savedAccounts.capture());
        assertThat(savedAccounts.getAllValues().get(0).getBalance()).isEqualByComparingTo("-250");

        ArgumentCaptor<Movement> mirroredMovement = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(mirroredMovement.capture());
        assertThat(mirroredMovement.getValue().getAccountId()).isEqualTo(propertyAccount.getId());
        assertThat(mirroredMovement.getValue().getDirection()).isEqualTo(MovementDirection.CREDIT);
        assertThat(mirroredMovement.getValue().getAmount().value()).isEqualByComparingTo("250");
        assertThat(mirroredMovement.getValue().getType()).isEqualTo(MovementType.INSTALLMENT);

        assertThat(savedAccounts.getAllValues().get(1).getBalance()).isEqualByComparingTo("250");
    }

    @Test
    void a_credit_on_a_unit_account_is_mirrored_as_a_debit_on_its_property_account() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        Account unitAccount = Account.create(AccountId.newId(), unitId, AccountType.UNIT);
        Account propertyAccount = Account.create(AccountId.newId(), propertyId, AccountType.PROPERTY);
        Movement payment = Movement.create(MovementId.newId(), unitAccount.getId(), Instant.parse("2027-01-01T00:00:00Z"),
                MovementType.PAYMENT, MovementDirection.CREDIT, Amount.of(new BigDecimal("500")), "Paiement", null);

        when(unitDirectoryPort.resolvePropertyId(unitId)).thenReturn(propertyId);
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.of(propertyAccount));

        newService().postMovement(unitAccount, payment);

        ArgumentCaptor<Movement> mirroredMovement = ArgumentCaptor.forClass(Movement.class);
        verify(movementRepository).save(mirroredMovement.capture());
        assertThat(mirroredMovement.getValue().getDirection()).isEqualTo(MovementDirection.DEBIT);
    }

    @Test
    void a_movement_on_a_property_account_is_not_mirrored() {
        Account propertyAccount = Account.create(AccountId.newId(), EntityId.newId(), AccountType.PROPERTY);
        Movement movement = Movement.create(MovementId.newId(), propertyAccount.getId(), Instant.now(),
                MovementType.PAYMENT, MovementDirection.CREDIT, Amount.of(BigDecimal.TEN), "Paiement", null);

        newService().postMovement(propertyAccount, movement);

        verify(accountRepository).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void mirroring_onto_a_missing_property_account_is_rejected() {
        EntityId unitId = EntityId.newId();
        EntityId propertyId = EntityId.newId();
        Account unitAccount = Account.create(AccountId.newId(), unitId, AccountType.UNIT);
        Movement movement = Movement.create(MovementId.newId(), unitAccount.getId(), Instant.now(),
                MovementType.PAYMENT, MovementDirection.CREDIT, Amount.of(BigDecimal.TEN), "Paiement", null);

        when(unitDirectoryPort.resolvePropertyId(unitId)).thenReturn(propertyId);
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().postMovement(unitAccount, movement))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
