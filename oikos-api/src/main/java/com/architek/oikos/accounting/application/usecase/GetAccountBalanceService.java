package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.BalanceView;
import com.architek.oikos.accounting.application.port.in.GetAccountBalanceUseCase;
import com.architek.oikos.accounting.application.query.GetAccountBalanceQuery;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;

/**
 * Balance is persisted on Account (kept in sync with every Movement by
 * AccountBalanceService), so reading it is a direct lookup.
 */
@Component
public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountRepository accountRepository;

    public GetAccountBalanceService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceView getBalance(GetAccountBalanceQuery query) {
        Account account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));

        return new BalanceView(query.accountId(), account.getBalance());
    }
}
