package com.architek.oikos.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JournalEntryJpaRepository extends JpaRepository<JournalEntryEntity, UUID> {

    Page<JournalEntryEntity> findAllByPropertyIdOrderByPieceDateDesc(UUID propertyId, Pageable pageable);

    List<JournalEntryEntity> findAllByPeriodId(UUID periodId);

    /**
     * Matches via the lines, not the entry-level treasuryAccountId: a
     * treasury-to-treasury transfer (OD journal) has two treasury lines and
     * no single "the" treasuryAccountId (JournalEntry.draft() forbids one on
     * a non-TREASURY-type journal), so it must still show up on both
     * accounts' operations pages.
     */
    @Query("""
            select distinct j from JournalEntryEntity j
            join j.lines l
            where j.propertyId = :propertyId
              and l.ledgerAccountId = :treasuryAccountId
              and (:pieceDateFrom is null or j.pieceDate >= :pieceDateFrom)
              and (:pieceDateTo is null or j.pieceDate <= :pieceDateTo)
              and (:search is null or lower(j.externalReference) like lower(concat('%', :search, '%')))
              and (:status is null or j.status = :status)
            order by j.pieceDate desc
            """)
    Page<JournalEntryEntity> searchByTreasuryAccount(@Param("propertyId") UUID propertyId,
                                                       @Param("treasuryAccountId") UUID treasuryAccountId,
                                                       @Param("pieceDateFrom") LocalDate pieceDateFrom,
                                                       @Param("pieceDateTo") LocalDate pieceDateTo,
                                                       @Param("search") String search,
                                                       @Param("status") String status,
                                                       Pageable pageable);

    /** Net balance (CREDIT minus DEBIT, POSTED only) of one account for one auxiliary unit. */
    @Query("""
            select coalesce(sum(case when l.direction = 'CREDIT' then l.amount else -l.amount end), 0)
            from JournalEntryLineEntity l join l.journalEntry j
            where j.propertyId = :propertyId and j.status = 'POSTED'
              and l.ledgerAccountId = :accountId and l.auxiliaryUnitId = :unitId
            """)
    BigDecimal sumNetAmountForAuxiliaryUnit(@Param("propertyId") UUID propertyId,
                                             @Param("accountId") UUID accountId, @Param("unitId") UUID unitId);

    /** Same balance as above, grouped by auxiliary unit, keeping only the strictly positive ones -
     * the property-wide regularisation sweep's starting point. */
    @Query("""
            select l.auxiliaryUnitId as unitId,
                   sum(case when l.direction = 'CREDIT' then l.amount else -l.amount end) as amount
            from JournalEntryLineEntity l join l.journalEntry j
            where j.propertyId = :propertyId and j.status = 'POSTED'
              and l.ledgerAccountId = :accountId and l.auxiliaryUnitId is not null
            group by l.auxiliaryUnitId
            having sum(case when l.direction = 'CREDIT' then l.amount else -l.amount end) > 0
            """)
    List<AuxiliaryUnitBalanceProjection> sumNetAmountGroupedByAuxiliaryUnit(@Param("propertyId") UUID propertyId,
                                                                             @Param("accountId") UUID accountId);

    interface AuxiliaryUnitBalanceProjection {
        UUID getUnitId();

        BigDecimal getAmount();
    }
}
