package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.port.in.GetAccountUseCase;
import com.architek.oikos.accounting.application.query.GetAccountQuery;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.repository.AccountRepository;

@Component
public class GetAccountService implements GetAccountUseCase {

    private final AccountRepository accountRepository;

    public GetAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountView getAccount(GetAccountQuery query) {
        return accountRepository.findById(query.id())
                .map(AccountView::from)
                .orElseThrow(() -> new AccountNotFoundException(query.id()));
    }
}
