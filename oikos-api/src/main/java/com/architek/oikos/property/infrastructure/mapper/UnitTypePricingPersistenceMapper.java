package com.architek.oikos.property.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.valueobject.Price;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;
import com.architek.oikos.property.infrastructure.persistence.UnitTypePricingEntity;

@Mapper(componentModel = "spring")
public interface UnitTypePricingPersistenceMapper {

    default UnitTypePricingEntity toEntity(UnitTypePricing unitTypePricing) {
        return toEntity(unitTypePricing, new UnitTypePricingEntity());
    }

    default UnitTypePricingEntity toEntity(UnitTypePricing unitTypePricing, UnitTypePricingEntity entity) {
        entity.setId(unitTypePricing.getId().asUuid());
        entity.setPropertyId(unitTypePricing.getPropertyId().asUuid());
        entity.setUnitTypeId(unitTypePricing.getUnitTypeId().asUuid());
        entity.setPrice(unitTypePricing.getPrice().value());
        return entity;
    }

    default UnitTypePricing toDomain(UnitTypePricingEntity entity) {
        return UnitTypePricing.reconstruct(UnitTypePricingId.of(entity.getId()), PropertyId.of(entity.getPropertyId()),
                UnitTypeDefinitionId.of(entity.getUnitTypeId()), Price.of(entity.getPrice()));
    }
}
