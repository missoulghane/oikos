package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.LedgerAccount;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountNumber;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.infrastructure.persistence.LedgerAccountEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface LedgerAccountPersistenceMapper {

    default LedgerAccountEntity toEntity(LedgerAccount account) {
        return toEntity(account, new LedgerAccountEntity());
    }

    default LedgerAccountEntity toEntity(LedgerAccount account, LedgerAccountEntity entity) {
        entity.setId(account.getId().asUuid());
        entity.setPropertyId(account.getPropertyId().map(EntityId::value).orElse(null));
        entity.setUnitId(account.getUnitId().map(EntityId::value).orElse(null));
        entity.setAccountNumber(account.getAccountNumber().value());
        entity.setLabel(account.getLabel());
        entity.setAccountClass(account.getAccountClass());
        entity.setNature(account.getNature().name());
        entity.setCollective(account.isCollective());
        entity.setRole(account.getRole().map(Enum::name).orElse(null));
        entity.setActive(account.isActive());
        entity.setBalance(account.getBalance());
        return entity;
    }

    default LedgerAccount toDomain(LedgerAccountEntity entity) {
        return LedgerAccount.reconstruct(LedgerAccountId.of(entity.getId()),
                entity.getPropertyId() == null ? null : EntityId.of(entity.getPropertyId()),
                entity.getUnitId() == null ? null : EntityId.of(entity.getUnitId()),
                AccountNumber.of(entity.getAccountNumber()), entity.getLabel(), entity.getAccountClass(),
                AccountNature.valueOf(entity.getNature()), entity.isCollective(),
                entity.getRole() == null ? null : AccountRole.valueOf(entity.getRole()), entity.isActive(),
                entity.getBalance());
    }
}
