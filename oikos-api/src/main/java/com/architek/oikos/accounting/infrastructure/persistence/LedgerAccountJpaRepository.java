package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerAccountJpaRepository extends JpaRepository<LedgerAccountEntity, UUID> {

    Optional<LedgerAccountEntity> findByPropertyIdIsNullAndRole(String role);

    Optional<LedgerAccountEntity> findByPropertyIdAndUnitIdIsNullAndRole(UUID propertyId, String role);

    Optional<LedgerAccountEntity> findByPropertyIdAndUnitIdAndRole(UUID propertyId, UUID unitId, String role);

    @Query("select a from LedgerAccountEntity a where a.propertyId is null or a.propertyId = :propertyId")
    List<LedgerAccountEntity> findAllVisibleToProperty(@Param("propertyId") UUID propertyId);

    /**
     * Atomic SQL-level increment - safe under concurrent posts touching the
     * same account (e.g. the cash account). flushAutomatically is required:
     * without it, a bulk @Modifying query does not flush pending changes
     * first, and clearAutomatically then silently drops them (e.g. the
     * JournalEntry's not-yet-flushed DRAFT-&gt;POSTED status update made
     * moments earlier in PostJournalEntryService, in the same transaction).
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update LedgerAccountEntity a set a.balance = a.balance + :delta where a.id = :id")
    void incrementBalance(@Param("id") UUID id, @Param("delta") BigDecimal delta);
}
