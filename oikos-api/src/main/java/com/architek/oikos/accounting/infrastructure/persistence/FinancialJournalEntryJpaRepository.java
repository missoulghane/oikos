package com.architek.oikos.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.architek.oikos.accounting.domain.valueobject.FinancialEntryType;

public interface FinancialJournalEntryJpaRepository extends JpaRepository<FinancialJournalEntryEntity, UUID> {

    @Query("""
            select e from FinancialJournalEntryEntity e
            where e.financialAccountId in :financialAccountIds
              and (:exerciseId is null or e.exerciseId = :exerciseId)
              and (:type is null or e.type = :type)
              and (:dateFrom is null or e.date >= :dateFrom)
              and (:dateTo is null or e.date <= :dateTo)
            """)
    Page<FinancialJournalEntryEntity> search(@Param("financialAccountIds") List<UUID> financialAccountIds,
                                              @Param("exerciseId") UUID exerciseId,
                                              @Param("type") FinancialEntryType type,
                                              @Param("dateFrom") LocalDate dateFrom,
                                              @Param("dateTo") LocalDate dateTo,
                                              Pageable pageable);
}
