package com.architek.oikos.property.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public interface UnitTypeDefinitionRepository {

    UnitTypeDefinition save(UnitTypeDefinition unitTypeDefinition);

    Optional<UnitTypeDefinition> findById(UnitTypeDefinitionId id);

    Optional<UnitTypeDefinition> findByPropertyIdAndName(PropertyId propertyId, String name);

    List<UnitTypeDefinition> findAllByPropertyId(PropertyId propertyId);

    boolean existsByPropertyIdAndName(PropertyId propertyId, String name);

    void deleteById(UnitTypeDefinitionId id);
}
