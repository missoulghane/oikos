package com.architek.oikos.accounting.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.repository.AccountRepository;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.infrastructure.mapper.AccountPersistenceMapper;
import com.architek.oikos.accounting.infrastructure.persistence.AccountEntity;
import com.architek.oikos.accounting.infrastructure.persistence.AccountJpaRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = jpaRepository.findById(account.getId().asUuid()).orElseGet(AccountEntity::new);
        mapper.toEntity(account, entity);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.asUuid()).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByHolderId(EntityId holderId, AccountType accountType) {
        return jpaRepository.findByHolderIdAndAccountType(holderId.value(), accountType).map(mapper::toDomain);
    }

    @Override
    public boolean existsByHolderId(EntityId holderId, AccountType accountType) {
        return jpaRepository.existsByHolderIdAndAccountType(holderId.value(), accountType);
    }
}
