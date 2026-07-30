package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.UnitAccountAllocation;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountAllocationId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountAllocationEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface UnitAccountAllocationPersistenceMapper {

    default UnitAccountAllocationEntity toEntity(UnitAccountAllocation allocation) {
        return toEntity(allocation, new UnitAccountAllocationEntity());
    }

    default UnitAccountAllocationEntity toEntity(UnitAccountAllocation allocation, UnitAccountAllocationEntity entity) {
        entity.setId(allocation.getId().asUuid());
        entity.setUnitAccountId(allocation.getUnitAccountId().asUuid());
        entity.setDebitMovementId(allocation.getDebitMovementId().asUuid());
        entity.setCreditMovementId(allocation.getCreditMovementId().asUuid());
        entity.setAmount(allocation.getAmount().value());
        entity.setAllocatedDate(allocation.getAllocatedDate());
        entity.setAllocatedByUserId(allocation.getAllocatedByUserId().value());
        return entity;
    }

    default UnitAccountAllocation toDomain(UnitAccountAllocationEntity entity) {
        return UnitAccountAllocation.reconstruct(UnitAccountAllocationId.of(entity.getId()),
                UnitAccountId.of(entity.getUnitAccountId()), UnitAccountMovementId.of(entity.getDebitMovementId()),
                UnitAccountMovementId.of(entity.getCreditMovementId()), Amount.of(entity.getAmount()),
                entity.getAllocatedDate(), EntityId.of(entity.getAllocatedByUserId()));
    }
}
