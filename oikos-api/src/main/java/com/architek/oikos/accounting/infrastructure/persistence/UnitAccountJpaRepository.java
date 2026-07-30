package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitAccountJpaRepository extends JpaRepository<UnitAccountEntity, UUID> {

    Optional<UnitAccountEntity> findByUnitId(UUID unitId);

    boolean existsByUnitId(UUID unitId);

    List<UnitAccountEntity> findAllByPropertyId(UUID propertyId);
}
