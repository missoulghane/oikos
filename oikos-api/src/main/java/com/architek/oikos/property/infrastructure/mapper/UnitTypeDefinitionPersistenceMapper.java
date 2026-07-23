package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.infrastructure.persistence.UnitTypeDefinitionEntity;

@Mapper(componentModel = "spring")
public interface UnitTypeDefinitionPersistenceMapper {

    default UnitTypeDefinitionEntity toEntity(UnitTypeDefinition unitTypeDefinition) {
        return toEntity(unitTypeDefinition, new UnitTypeDefinitionEntity());
    }

    default UnitTypeDefinitionEntity toEntity(UnitTypeDefinition unitTypeDefinition, UnitTypeDefinitionEntity entity) {
        entity.setId(unitTypeDefinition.getId().asUuid());
        entity.setPropertyId(unitTypeDefinition.getPropertyId().asUuid());
        entity.setName(unitTypeDefinition.getName());
        return entity;
    }

    default UnitTypeDefinition toDomain(UnitTypeDefinitionEntity entity) {
        return UnitTypeDefinition.reconstruct(UnitTypeDefinitionId.of(entity.getId()),
                PropertyId.of(entity.getPropertyId()), entity.getName());
    }
}
