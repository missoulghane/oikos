package com.architek.oikos.installment.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.infrastructure.persistence.AllocationEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface AllocationPersistenceMapper {

    default AllocationEntity toEntity(Allocation allocation) {
        return toEntity(allocation, new AllocationEntity());
    }

    default AllocationEntity toEntity(Allocation allocation, AllocationEntity entity) {
        entity.setId(allocation.getId().asUuid());
        entity.setMovementId(allocation.getMovementId().value());
        entity.setInstallmentId(allocation.getInstallmentId().asUuid());
        entity.setAllocatedAmount(allocation.getAllocatedAmount().value());
        return entity;
    }

    default Allocation toDomain(AllocationEntity entity) {
        return Allocation.reconstruct(AllocationId.of(entity.getId()), EntityId.of(entity.getMovementId()),
                InstallmentId.of(entity.getInstallmentId()), Amount.of(entity.getAllocatedAmount()));
    }
}
