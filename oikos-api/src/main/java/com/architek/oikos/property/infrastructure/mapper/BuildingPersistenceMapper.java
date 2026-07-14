package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.infrastructure.persistence.BuildingEntity;

@Mapper(componentModel = "spring")
public interface BuildingPersistenceMapper {

    default BuildingEntity toEntity(Building building) {
        return toEntity(building, new BuildingEntity());
    }

    default BuildingEntity toEntity(Building building, BuildingEntity entity) {
        entity.setId(building.getId().asUuid());
        entity.setPropertyId(building.getPropertyId().asUuid());
        entity.setName(building.getName());
        entity.setFloorCount(building.getFloorCount());
        return entity;
    }

    default Building toDomain(BuildingEntity entity) {
        return Building.reconstruct(BuildingId.of(entity.getId()), PropertyId.of(entity.getPropertyId()),
                entity.getName(), entity.getFloorCount());
    }
}
