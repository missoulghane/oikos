package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.repository.LedgerAccountNumberSequenceRepository;
import com.architek.oikos.accounting.domain.repository.LedgerAccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class ProvisionPropertyCashAccountServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerAccountNumberSequenceRepository sequenceRepository;

    private ProvisionPropertyCashAccountService newService() {
        return new ProvisionPropertyCashAccountService(ledgerAccountRepository, sequenceRepository);
    }

    @Test
    void provisions_a_cash_account_numbered_from_the_516100_prefix() {
        EntityId propertyId = EntityId.newId();
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("516100"))).thenReturn(1);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().provision(propertyId);

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        LedgerAccount account = captor.getValue();
        assertThat(account.getAccountNumber().value()).isEqualTo("51610001");
        assertThat(account.getPropertyId()).contains(propertyId);
        assertThat(account.getUnitId()).isEmpty();
        assertThat(account.getRole()).contains(AccountRole.CASH);
        assertThat(account.isCollective()).isFalse();
    }

    @Test
    void a_second_cash_account_for_the_same_property_gets_the_next_increment() {
        EntityId propertyId = EntityId.newId();
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("516100"))).thenReturn(2);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().provision(propertyId);

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountNumber().value()).isEqualTo("51610002");
    }
}
