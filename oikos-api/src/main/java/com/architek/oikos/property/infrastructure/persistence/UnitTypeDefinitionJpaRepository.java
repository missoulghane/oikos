package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitTypeDefinitionJpaRepository extends JpaRepository<UnitTypeDefinitionEntity, UUID> {

    List<UnitTypeDefinitionEntity> findByPropertyId(UUID propertyId);

    Optional<UnitTypeDefinitionEntity> findByPropertyIdAndName(UUID propertyId, String name);

    boolean existsByPropertyIdAndName(UUID propertyId, String name);
}
