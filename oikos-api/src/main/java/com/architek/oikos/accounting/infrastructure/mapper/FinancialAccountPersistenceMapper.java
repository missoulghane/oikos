package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.FinancialAccount;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.accounting.infrastructure.persistence.FinancialAccountEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface FinancialAccountPersistenceMapper {

    default FinancialAccountEntity toEntity(FinancialAccount account) {
        return toEntity(account, new FinancialAccountEntity());
    }

    default FinancialAccountEntity toEntity(FinancialAccount account, FinancialAccountEntity entity) {
        entity.setId(account.getId().asUuid());
        entity.setPropertyId(account.getPropertyId().value());
        entity.setName(account.getName());
        entity.setType(account.getType());
        entity.setCurrency(account.getCurrency());
        entity.setBalance(account.getBalance());
        entity.setStatus(account.getStatus());
        return entity;
    }

    default FinancialAccount toDomain(FinancialAccountEntity entity) {
        return FinancialAccount.reconstruct(FinancialAccountId.of(entity.getId()), EntityId.of(entity.getPropertyId()),
                entity.getName(), entity.getType(), entity.getCurrency(), entity.getBalance(), entity.getStatus());
    }
}
