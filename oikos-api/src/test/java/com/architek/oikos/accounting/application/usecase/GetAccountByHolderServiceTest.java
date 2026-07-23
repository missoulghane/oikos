package com.architek.oikos.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class GetAccountByHolderServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private GetAccountByHolderService newService() {
        return new GetAccountByHolderService(accountRepository);
    }

    @Test
    void returns_the_account_for_the_given_holder_and_type() {
        EntityId propertyId = EntityId.newId();
        Account account = Account.reconstruct(AccountId.newId(), propertyId, AccountType.PROPERTY, new BigDecimal("750"));
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.of(account));

        AccountView view = newService().getAccount(new GetAccountByHolderQuery(propertyId, AccountType.PROPERTY));

        assertThat(view.holderId()).isEqualTo(propertyId);
        assertThat(view.accountType()).isEqualTo(AccountType.PROPERTY);
    }

    @Test
    void getting_the_account_of_a_holder_without_one_throws() {
        EntityId propertyId = EntityId.newId();
        when(accountRepository.findByHolderId(propertyId, AccountType.PROPERTY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getAccount(new GetAccountByHolderQuery(propertyId, AccountType.PROPERTY)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
