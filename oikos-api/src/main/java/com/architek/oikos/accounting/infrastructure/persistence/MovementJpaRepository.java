package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementJpaRepository extends JpaRepository<MovementEntity, UUID> {

    List<MovementEntity> findAllByAccountId(UUID accountId);

    Page<MovementEntity> findAllByAccountId(UUID accountId, Pageable pageable);
}
