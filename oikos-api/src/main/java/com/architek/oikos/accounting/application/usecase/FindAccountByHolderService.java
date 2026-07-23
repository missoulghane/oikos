package com.architek.oikos.accounting.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.port.in.FindAccountByHolderUseCase;
import com.architek.oikos.accounting.application.query.GetAccountByHolderQuery;
import com.architek.oikos.accounting.domain.repository.AccountRepository;

@Component
public class FindAccountByHolderService implements FindAccountByHolderUseCase {

    private final AccountRepository accountRepository;

    public FindAccountByHolderService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountView> findAccount(GetAccountByHolderQuery query) {
        return accountRepository.findByHolderId(query.holderId(), query.accountType()).map(AccountView::from);
    }
}
