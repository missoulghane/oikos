package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, UUID> {

    Page<JournalEntryEntity> findAllByPropertyIdOrderByPieceDateDesc(UUID propertyId, Pageable pageable);

    List<JournalEntryEntity> findAllByPeriodId(UUID periodId);
}
