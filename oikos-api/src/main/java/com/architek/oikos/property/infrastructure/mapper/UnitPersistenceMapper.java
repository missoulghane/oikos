package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.infrastructure.persistence.UnitEntity;

@Mapper(componentModel = "spring")
public interface UnitPersistenceMapper {

    default UnitEntity toEntity(Unit unit) {
        return toEntity(unit, new UnitEntity());
    }

    default UnitEntity toEntity(Unit unit, UnitEntity entity) {
        entity.setId(unit.getId().asUuid());
        entity.setBuildingId(unit.getBuildingId().asUuid());
        entity.setUnitNumber(unit.getUnitNumber());
        entity.setUnitType(unit.getUnitType());
        entity.setShares(unit.getShares().value());
        return entity;
    }

    default Unit toDomain(UnitEntity entity) {
        return Unit.reconstruct(UnitId.of(entity.getId()), BuildingId.of(entity.getBuildingId()), entity.getUnitNumber(),
                entity.getUnitType(), Shares.of(entity.getShares()));
    }
}
