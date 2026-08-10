package com.architek.oikos.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SequencePieceJpaRepository extends JpaRepository<SequencePieceEntity, SequencePieceEntity.Key> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SequencePieceEntity s where s.propertyId = :propertyId and s.exerciseId = :exerciseId and s.journalCode = :journalCode")
    Optional<SequencePieceEntity> findForUpdate(@Param("propertyId") UUID propertyId,
                                                 @Param("exerciseId") UUID exerciseId,
                                                 @Param("journalCode") String journalCode);
}
