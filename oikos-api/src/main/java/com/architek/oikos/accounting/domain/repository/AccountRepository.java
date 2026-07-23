package com.architek.oikos.accounting.domain.repository;

import java.util.Optional;

import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(AccountId id);

    Optional<Account> findByHolderId(EntityId holderId, AccountType accountType);

    boolean existsByHolderId(EntityId holderId, AccountType accountType);
}
