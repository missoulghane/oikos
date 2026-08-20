package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.valueobject.ProjectedBudget;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.infrastructure.persistence.PropertyEntity;

@Mapper(componentModel = "spring")
public interface PropertyPersistenceMapper {

    default PropertyEntity toEntity(Property property) {
        return toEntity(property, new PropertyEntity());
    }

    default PropertyEntity toEntity(Property property, PropertyEntity entity) {
        entity.setId(property.getId().asUuid());
        entity.setName(property.getName());
        entity.setAddress(property.getAddress());
        entity.setCity(property.getCity().orElse(null));
        entity.setDuesCalculationMode(property.getDuesCalculationMode());
        entity.setProjectedBudget(property.getProjectedBudget().map(ProjectedBudget::value).orElse(null));
        return entity;
    }

    default Property toDomain(PropertyEntity entity) {
        return Property.reconstruct(PropertyId.of(entity.getId()), entity.getName(), entity.getAddress(),
                entity.getCity(), entity.getDuesCalculationMode(),
                entity.getProjectedBudget() != null ? ProjectedBudget.of(entity.getProjectedBudget()) : null);
    }
}
