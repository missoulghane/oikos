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

    List<InstallmentEntity> findAllByAccountId(UUID accountId);

    List<InstallmentEntity> findAllByUnitId(UUID unitId);

    List<InstallmentEntity> findAllByInstallmentCallId(UUID installmentCallId);

    /**
     * Status (NOT_PAID/PARTIALLY_PAID/PAID/OVERDUE) is never stored (RG011),
     * so each status branch is expressed here from amount, the correlated sum
     * of its allocations, and dueDate vs today - mirroring
     * InstallmentStatusCalculator exactly (PAID wins regardless of due date,
     * then OVERDUE takes priority over PARTIALLY_PAID). hasStatusFilter lets
     * an empty status set mean "no filter" without a separate query.
     */
    @Query("""
            select i from InstallmentEntity i
            where i.unitId in :unitIds
              and (:dueDateFrom is null or i.dueDate >= :dueDateFrom)
              and (:dueDateTo is null or i.dueDate <= :dueDateTo)
              and (:hasStatusFilter = false or (
                    (:wantPaid = true and (i.amount - coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0)) <= 0)
                 or (:wantOverdue = true and (i.amount - coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0)) > 0 and i.dueDate < :today)
                 or (:wantPartiallyPaid = true and (i.amount - coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0)) > 0 and i.dueDate >= :today and coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0) > 0)
                 or (:wantNotPaid = true and (i.amount - coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0)) > 0 and i.dueDate >= :today and coalesce((select sum(a.allocatedAmount) from AllocationEntity a where a.installmentId = i.id), 0) <= 0)
              ))
            """)
    Page<InstallmentEntity> search(@Param("unitIds") List<UUID> unitIds,
                                    @Param("dueDateFrom") LocalDate dueDateFrom,
                                    @Param("dueDateTo") LocalDate dueDateTo,
                                    @Param("hasStatusFilter") boolean hasStatusFilter,
                                    @Param("wantNotPaid") boolean wantNotPaid,
                                    @Param("wantPartiallyPaid") boolean wantPartiallyPaid,
                                    @Param("wantPaid") boolean wantPaid,
                                    @Param("wantOverdue") boolean wantOverdue,
                                    @Param("today") LocalDate today,
                                    Pageable pageable);
}
