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
     */
    @Query("""
            select i from InstallmentEntity i
            where i.unitId in :unitIds
              and (:dueDateFrom is null or i.dueDate >= :dueDateFrom)
              and (:dueDateTo is null or i.dueDate <= :dueDateTo)
              and (:hasStatusFilter = false or (
                    (:wantNotSettled = true and i.outstandingAmount >= i.amount)
                 or (:wantPartiallySettled = true and i.outstandingAmount > 0 and i.outstandingAmount < i.amount)
                 or (:wantSettled = true and i.outstandingAmount <= 0)
              ))
              and (:installmentCallId is null or i.installmentCallId = :installmentCallId)
              and (:hideNotYetDueAsOf is null
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
}
