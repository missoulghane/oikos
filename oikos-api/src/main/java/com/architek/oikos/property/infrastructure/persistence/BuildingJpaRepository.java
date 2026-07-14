package com.architek.oikos.property.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingJpaRepository extends JpaRepository<BuildingEntity, UUID> {

    Page<BuildingEntity> findByPropertyId(UUID propertyId, Pageable pageable);
}
