package com.architek.oikos.accounting.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.infrastructure.persistence.MovementEntity;

@Mapper(componentModel = "spring")
public interface MovementPersistenceMapper {

    default MovementEntity toEntity(Movement movement) {
        return toEntity(movement, new MovementEntity());
    }

    default MovementEntity toEntity(Movement movement, MovementEntity entity) {
        entity.setId(movement.getId().asUuid());
        entity.setAccountId(movement.getAccountId().asUuid());
        entity.setOccurredOn(movement.getOccurredOn());
        entity.setType(movement.getType());
        entity.setDirection(movement.getDirection());
        entity.setAmount(movement.getAmount().value());
        entity.setLabel(movement.getLabel());
        entity.setBusinessReference(movement.getBusinessReference());
        return entity;
    }

    default Movement toDomain(MovementEntity entity) {
        return Movement.reconstruct(MovementId.of(entity.getId()), AccountId.of(entity.getAccountId()),
                entity.getOccurredOn(), entity.getType(), entity.getDirection(), Amount.of(entity.getAmount()),
                entity.getLabel(), entity.getBusinessReference());
    }
}
