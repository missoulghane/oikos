package com.architek.oikos.property.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitJpaRepository extends JpaRepository<UnitEntity, UUID> {

    Page<UnitEntity> findByBuildingId(UUID buildingId, Pageable pageable);
}
