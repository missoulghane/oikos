package com.architek.oikos.property.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitOwnershipJpaRepository extends JpaRepository<UnitOwnershipEntity, UUID> {

    List<UnitOwnershipEntity> findByUnitId(UUID unitId);

    List<UnitOwnershipEntity> findByUnitIdIn(List<UUID> unitIds);

    List<UnitOwnershipEntity> findByPartyId(UUID partyId);

    boolean existsByUnitIdAndPartyId(UUID unitId, UUID partyId);
}
