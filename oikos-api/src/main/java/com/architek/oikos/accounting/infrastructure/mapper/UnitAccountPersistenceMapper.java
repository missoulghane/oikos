package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.UnitAccount;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface UnitAccountPersistenceMapper {

    default UnitAccountEntity toEntity(UnitAccount account) {
        return toEntity(account, new UnitAccountEntity());
    }

    default UnitAccountEntity toEntity(UnitAccount account, UnitAccountEntity entity) {
        entity.setId(account.getId().asUuid());
        entity.setUnitId(account.getUnitId().value());
        entity.setPropertyId(account.getPropertyId().value());
        entity.setBalance(account.getBalance());
        entity.setLastUpdatedDate(account.getLastUpdatedDate());
        return entity;
    }

    default UnitAccount toDomain(UnitAccountEntity entity) {
        return UnitAccount.reconstruct(UnitAccountId.of(entity.getId()), EntityId.of(entity.getUnitId()),
                EntityId.of(entity.getPropertyId()), entity.getBalance(), entity.getLastUpdatedDate());
    }
}
