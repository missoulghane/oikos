package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitAccountAllocationJpaRepository extends JpaRepository<UnitAccountAllocationEntity, UUID> {

    List<UnitAccountAllocationEntity> findAllByUnitAccountId(UUID unitAccountId);
}
