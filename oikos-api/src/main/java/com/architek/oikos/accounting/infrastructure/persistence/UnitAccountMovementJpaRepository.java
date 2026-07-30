package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitAccountMovementJpaRepository extends JpaRepository<UnitAccountMovementEntity, UUID> {

    Page<UnitAccountMovementEntity> findAllByUnitAccountId(UUID unitAccountId, Pageable pageable);

    List<UnitAccountMovementEntity> findAllByUnitAccountId(UUID unitAccountId);
}
