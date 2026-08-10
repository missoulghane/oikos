package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerAccountNumberSequenceJpaRepository
        extends JpaRepository<LedgerAccountNumberSequenceEntity, LedgerAccountNumberSequenceEntity.Key> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from LedgerAccountNumberSequenceEntity s where s.propertyId = :propertyId and s.numberPrefix = :numberPrefix")
    Optional<LedgerAccountNumberSequenceEntity> findForUpdate(@Param("propertyId") UUID propertyId,
                                                               @Param("numberPrefix") String numberPrefix);
}
