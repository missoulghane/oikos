package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.infrastructure.persistence.UnitAccountMovementEntity;
import com.architek.oikos.shared.domain.valueobject.Amount;

@Mapper(componentModel = "spring")
public interface UnitAccountMovementPersistenceMapper {

    default UnitAccountMovementEntity toEntity(UnitAccountMovement movement) {
        return toEntity(movement, new UnitAccountMovementEntity());
    }

    default UnitAccountMovementEntity toEntity(UnitAccountMovement movement, UnitAccountMovementEntity entity) {
        entity.setId(movement.getId().asUuid());
        entity.setExerciseId(movement.getExerciseId().asUuid());
        entity.setUnitAccountId(movement.getUnitAccountId().asUuid());
        entity.setDate(movement.getDate());
        entity.setType(movement.getType());
        entity.setDirection(movement.getDirection());
        entity.setAmount(movement.getAmount().value());
        entity.setBusinessReference(movement.getBusinessReference());
        entity.setLabel(movement.getLabel());
        entity.setReason(movement.getReason());
        return entity;
    }

    default UnitAccountMovement toDomain(UnitAccountMovementEntity entity) {
        return UnitAccountMovement.reconstruct(UnitAccountMovementId.of(entity.getId()),
                AccountingExerciseId.of(entity.getExerciseId()), UnitAccountId.of(entity.getUnitAccountId()),
                entity.getDate(), entity.getType(), entity.getDirection(), Amount.of(entity.getAmount()),
                entity.getBusinessReference(), entity.getLabel(), entity.getReason());
    }
}
