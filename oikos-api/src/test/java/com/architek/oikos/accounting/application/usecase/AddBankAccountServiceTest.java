package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.AddBankAccountCommand;
import com.architek.oikos.accounting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.accounting.domain.exception.PropertyNotFoundException;
import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AddBankAccountServiceTest {

    @Mock
    private PropertyDirectoryPort propertyDirectoryPort;

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerAccountNumberSequenceRepository sequenceRepository;

    private AddBankAccountService newService() {
        return new AddBankAccountService(propertyDirectoryPort, ledgerAccountRepository, sequenceRepository);
    }

    @Test
    void provisions_a_bank_account_numbered_from_the_514100_prefix() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("514100"))).thenReturn(1);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBankAccountCommand(propertyId, "Attijariwafa Bank", "007780000123456789012345"));

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        LedgerAccount account = captor.getValue();
        assertThat(account.getAccountNumber().value()).isEqualTo("51410001");
        assertThat(account.getPropertyId()).contains(propertyId);
        assertThat(account.getUnitId()).isEmpty();
        assertThat(account.getRole()).contains(AccountRole.BANK);
        assertThat(account.isCollective()).isFalse();
        assertThat(account.getLabel()).isEqualTo("Attijariwafa Bank");
        assertThat(account.getBankAccountNumber()).contains("007780000123456789012345");
    }

    @Test
    void a_missing_property_is_rejected() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(false);

        assertThatThrownBy(() -> newService().add(new AddBankAccountCommand(propertyId, "Banque", null)))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void a_second_bank_account_for_the_same_property_is_allowed() {
        EntityId propertyId = EntityId.newId();
        when(propertyDirectoryPort.exists(propertyId)).thenReturn(true);
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("514100"))).thenReturn(2);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBankAccountCommand(propertyId, "Deuxieme banque", null));

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountNumber().value()).isEqualTo("51410002");
    }
}
