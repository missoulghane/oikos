package com.architek.oikos.property.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public interface UnitTypePricingRepository {

    UnitTypePricing save(UnitTypePricing unitTypePricing);

    Optional<UnitTypePricing> findByUnitTypeId(UnitTypeDefinitionId unitTypeId);

    List<UnitTypePricing> findAllByPropertyId(PropertyId propertyId);

    void deleteByUnitTypeId(UnitTypeDefinitionId unitTypeId);
}
