package com.architek.oikos.installment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.command.RecordDebitCommand;
import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.port.in.FindAccountByHolderUseCase;
import com.architek.oikos.accounting.application.port.in.RecordDebitUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class InstallmentAccountLedgerAdapterTest {

    @Mock
    private FindAccountByHolderUseCase findAccountByHolderUseCase;

    @Mock
    private RecordDebitUseCase recordDebitUseCase;

    private InstallmentAccountLedgerAdapter newAdapter() {
        return new InstallmentAccountLedgerAdapter(findAccountByHolderUseCase, recordDebitUseCase);
    }

    @Test
    void finds_the_unit_account_id_when_it_exists() {
        EntityId unitId = EntityId.newId();
        AccountId accountId = AccountId.newId();
        when(findAccountByHolderUseCase.findAccount(any()))
                .thenReturn(Optional.of(new AccountView(accountId, unitId, AccountType.UNIT, BigDecimal.ZERO)));

        var result = newAdapter().findUnitAccountId(unitId);

        assertThat(result).contains(accountId.value());
    }

    @Test
    void returns_empty_when_the_unit_has_no_account_yet() {
        EntityId unitId = EntityId.newId();
        when(findAccountByHolderUseCase.findAccount(any())).thenReturn(Optional.empty());

        var result = newAdapter().findUnitAccountId(unitId);

        assertThat(result).isEmpty();
    }

    @Test
    void property_account_exists_reflects_whether_the_lookup_succeeds() {
        EntityId propertyId = EntityId.newId();
        when(findAccountByHolderUseCase.findAccount(any())).thenReturn(Optional.empty());

        assertThat(newAdapter().propertyAccountExists(propertyId)).isFalse();
    }

    @Test
    void records_a_debit_through_the_accounting_use_case() {
        EntityId accountId = EntityId.newId();

        newAdapter().recordDebit(accountId, new BigDecimal("250"), "Appel de cotisation");

        verify(recordDebitUseCase).record(eq(new RecordDebitCommand(AccountId.of(accountId.value()),
                new BigDecimal("250"), "Appel de cotisation")));
    }
}
