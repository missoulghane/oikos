package com.architek.oikos.accounting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.port.in.GetAccountByHolderUseCase;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.domain.exception.AccountNotFoundException;
import com.architek.oikos.accounting.domain.repository.AccountRepository;

@Component
public class GetAccountByHolderService implements GetAccountByHolderUseCase {

    private final AccountRepository accountRepository;

    public GetAccountByHolderService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountView getAccount(GetAccountByHolderQuery query) {
        return accountRepository.findByHolderId(query.holderId(), query.accountType())
                .map(AccountView::from)
                .orElseThrow(() -> AccountNotFoundException.forHolder(query.holderId(), query.accountType()));
    }
}
