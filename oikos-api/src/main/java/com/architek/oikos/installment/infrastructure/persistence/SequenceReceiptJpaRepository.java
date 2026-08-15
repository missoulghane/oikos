package com.architek.oikos.installment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SequenceReceiptJpaRepository extends JpaRepository<SequenceReceiptEntity, SequenceReceiptEntity.Key> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SequenceReceiptEntity s where s.propertyId = :propertyId and s.year = :year")
    Optional<SequenceReceiptEntity> findForUpdate(@Param("propertyId") UUID propertyId, @Param("year") int year);
}
