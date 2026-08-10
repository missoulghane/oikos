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
class ProvisionUnitReceivableAccountServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    @Mock
    private LedgerAccountNumberSequenceRepository sequenceRepository;

    private ProvisionUnitReceivableAccountService newService() {
        return new ProvisionUnitReceivableAccountService(ledgerAccountRepository, sequenceRepository);
    }

    @Test
    void I6_provisions_a_dedicated_non_collective_receivable_account_numbered_from_the_341150_prefix() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("341150"))).thenReturn(1);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().provision(propertyId, unitId);

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        LedgerAccount account = captor.getValue();
        assertThat(account.getAccountNumber().value()).isEqualTo("34115001");
        assertThat(account.getPropertyId()).contains(propertyId);
        assertThat(account.getUnitId()).contains(unitId);
        assertThat(account.getRole()).contains(AccountRole.UNIT_RECEIVABLE);
        assertThat(account.isCollective()).isFalse();
        assertThat(account.requiresAuxiliary()).isFalse();
    }

    @Test
    void a_third_unit_of_the_same_property_gets_the_third_increment() {
        EntityId propertyId = EntityId.newId();
        EntityId unitId = EntityId.newId();
        when(sequenceRepository.allocateNextIncrement(eq(propertyId), eq("341150"))).thenReturn(3);
        when(ledgerAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().provision(propertyId, unitId);

        ArgumentCaptor<LedgerAccount> captor = ArgumentCaptor.forClass(LedgerAccount.class);
        verify(ledgerAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountNumber().value()).isEqualTo("34115003");
    }
}
