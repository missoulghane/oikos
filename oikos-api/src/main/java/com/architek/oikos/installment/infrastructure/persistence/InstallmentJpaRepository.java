package com.architek.oikos.installment.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstallmentJpaRepository extends JpaRepository<InstallmentEntity, UUID> {

    List<InstallmentEntity> findAllByUnitId(UUID unitId);

    List<InstallmentEntity> findAllByInstallmentCallId(UUID installmentCallId);

    void deleteAllByInstallmentCallId(UUID installmentCallId);

    /**
     * Status (NOT_SETTLED/PARTIALLY_SETTLED/SETTLED) is never stored as such,
     * so it is expressed here from outstandingAmount vs amount, mirroring
     * InstallmentStatusCalculator exactly. hasStatusFilter lets an empty
     * status set mean "no filter" without a separate query.
     *
     * <p>Every optional filter is cast explicitly, for the reason spelled out
     * on JournalEntryJpaRepository#searchByTreasuryAccount: a
     * {@code (:p is null or ...)} guard compiles to two parameter markers, and
     * the one inside {@code ? is null} has no surrounding expression to take a
     * type from, so PostgreSQL refuses to prepare the statement at all -
     * "could not determine data type of parameter". It is a listing whose
     * filters are all optional, so this fired as soon as one of them was left
     * unset - unticking "à échoir", most visibly.
     *
     * <p>H2 in MODE=PostgreSQL binds those parameters happily, which is why
     * the repository test above this one never saw it.
     * InstallmentSearchPostgresIntegrationTest runs every combination of
     * "filter not set" against a real PostgreSQL.
     */
    @Query("""
            select i from InstallmentEntity i
            where i.unitId in :unitIds
              and (cast(:dueDateFrom as LocalDate) is null or i.dueDate >= :dueDateFrom)
              and (cast(:dueDateTo as LocalDate) is null or i.dueDate <= :dueDateTo)
              and (:hasStatusFilter = false or (
                    (:wantNotSettled = true and i.outstandingAmount >= i.amount)
                 or (:wantPartiallySettled = true and i.outstandingAmount > 0 and i.outstandingAmount < i.amount)
                 or (:wantSettled = true and i.outstandingAmount <= 0)
              ))
              and (cast(:installmentCallId as java.util.UUID) is null or i.installmentCallId = :installmentCallId)
              and (cast(:hideNotYetDueAsOf as LocalDate) is null
                   or i.dueDate <= :hideNotYetDueAsOf
                   or i.outstandingAmount <= 0)
            """)
    Page<InstallmentEntity> search(@Param("unitIds") List<UUID> unitIds,
                                    @Param("dueDateFrom") LocalDate dueDateFrom,
                                    @Param("dueDateTo") LocalDate dueDateTo,
                                    @Param("hasStatusFilter") boolean hasStatusFilter,
                                    @Param("wantNotSettled") boolean wantNotSettled,
                                    @Param("wantPartiallySettled") boolean wantPartiallySettled,
                                    @Param("wantSettled") boolean wantSettled,
                                    @Param("installmentCallId") UUID installmentCallId,
                                    @Param("hideNotYetDueAsOf") LocalDate hideNotYetDueAsOf,
                                    Pageable pageable);

    /**
     * The two figures of the "à collecter" badge in one pass: how many
     * installments are unpaid (nothing received: outstanding >= amount, the
     * NOT_SETTLED of InstallmentStatusCalculator) and already due, and what
     * they add up to.
     *
     * <p>coalesce because sum() over no row is null, and a badge showing an
     * empty amount for a copropriété that owes nothing would be a blank where
     * "0" is the answer.
     */
    @Query("""
            select new com.architek.oikos.installment.infrastructure.persistence.InstallmentCollectionProjection(
                       count(i), coalesce(sum(i.outstandingAmount), 0))
            from InstallmentEntity i
            where i.unitId in :unitIds
              and i.outstandingAmount >= i.amount
              and i.dueDate <= :asOf
            """)
    InstallmentCollectionProjection summariseCollectible(@Param("unitIds") List<UUID> unitIds,
                                                          @Param("asOf") LocalDate asOf);
}
