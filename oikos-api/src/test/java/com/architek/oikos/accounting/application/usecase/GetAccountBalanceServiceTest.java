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

import com.architek.oikos.accounting.application.dto.BalanceView;
import com.architek.oikos.accounting.application.query.GetAccountBalanceQuery;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * RG010 (revised): balance is persisted on Account, kept in sync with every
 * movement by AccountBalanceService, so reading it is a direct lookup.
 */
@ExtendWith(MockitoExtension.class)
class GetAccountBalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private GetAccountBalanceService newService() {
        return new GetAccountBalanceService(accountRepository);
    }

    @Test
    void returns_the_account_s_persisted_balance() {
        AccountId accountId = AccountId.newId();
        Account account = Account.reconstruct(accountId, EntityId.newId(), AccountType.UNIT, new BigDecimal("750"));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BalanceView balance = newService().getBalance(new GetAccountBalanceQuery(accountId));

        assertThat(balance.balance()).isEqualByComparingTo("750");
    }

    @Test
    void getting_the_balance_of_an_unknown_account_is_rejected() {
        AccountId accountId = AccountId.newId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getBalance(new GetAccountBalanceQuery(accountId)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
