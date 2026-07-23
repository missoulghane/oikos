package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class FindAccountByHolderServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Test
    void returns_the_account_when_it_exists() {
        EntityId propertyId = EntityId.newId();
        Account account = Account.reconstruct(AccountId.newId(), propertyId, AccountType.PROPERTY, new BigDecimal("750"));
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.of(account));

        var result = new FindAccountByHolderService(accountRepository)
                .findAccount(new GetAccountByHolderQuery(propertyId, AccountType.PROPERTY));

        assertThat(result).isPresent();
        assertThat(result.get().holderId()).isEqualTo(propertyId);
    }

    @Test
    void returns_empty_when_no_account_exists_for_the_holder() {
        EntityId propertyId = EntityId.newId();
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.empty());

        var result = new FindAccountByHolderService(accountRepository)
                .findAccount(new GetAccountByHolderQuery(propertyId, AccountType.PROPERTY));

        assertThat(result).isEmpty();
    }
}
