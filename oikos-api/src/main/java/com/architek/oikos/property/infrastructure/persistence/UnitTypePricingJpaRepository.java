package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitTypePricingJpaRepository extends JpaRepository<UnitTypePricingEntity, UUID> {

    List<UnitTypePricingEntity> findByPropertyId(UUID propertyId);

    Optional<UnitTypePricingEntity> findByUnitTypeId(UUID unitTypeId);

    void deleteByUnitTypeId(UUID unitTypeId);
}
