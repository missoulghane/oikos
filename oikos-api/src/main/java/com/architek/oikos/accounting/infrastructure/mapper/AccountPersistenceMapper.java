package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.Account;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.infrastructure.persistence.AccountEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface AccountPersistenceMapper {

    default AccountEntity toEntity(Account account) {
        return toEntity(account, new AccountEntity());
    }

    default AccountEntity toEntity(Account account, AccountEntity entity) {
        entity.setId(account.getId().asUuid());
        entity.setHolderId(account.getHolderId().value());
        entity.setAccountType(account.getAccountType());
        entity.setBalance(account.getBalance());
        return entity;
    }

    default Account toDomain(AccountEntity entity) {
        return Account.reconstruct(AccountId.of(entity.getId()), EntityId.of(entity.getHolderId()),
                entity.getAccountType(), entity.getBalance());
    }
}
