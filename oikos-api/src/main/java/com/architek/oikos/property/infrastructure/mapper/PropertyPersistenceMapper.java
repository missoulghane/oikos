package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.Property;
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
        return entity;
    }

    default Property toDomain(PropertyEntity entity) {
        return Property.reconstruct(PropertyId.of(entity.getId()), entity.getName(), entity.getAddress());
    }
}
