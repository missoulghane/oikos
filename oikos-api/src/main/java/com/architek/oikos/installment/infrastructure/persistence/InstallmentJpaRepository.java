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

    /**
     * Status (NOT_PAID/OVERDUE) is never stored, so it is expressed here from
     * dueDate vs today, mirroring InstallmentStatusCalculator exactly.
     * hasStatusFilter lets an empty status set mean "no filter" without a
     * separate query.
     */
    @Query("""
            select i from InstallmentEntity i
            where i.unitId in :unitIds
              and (:dueDateFrom is null or i.dueDate >= :dueDateFrom)
              and (:dueDateTo is null or i.dueDate <= :dueDateTo)
              and (:hasStatusFilter = false or (
                    (:wantOverdue = true and i.dueDate < :today)
                 or (:wantNotPaid = true and i.dueDate >= :today)
              ))
            """)
    Page<InstallmentEntity> search(@Param("unitIds") List<UUID> unitIds,
                                    @Param("dueDateFrom") LocalDate dueDateFrom,
                                    @Param("dueDateTo") LocalDate dueDateTo,
                                    @Param("hasStatusFilter") boolean hasStatusFilter,
                                    @Param("wantNotPaid") boolean wantNotPaid,
                                    @Param("wantOverdue") boolean wantOverdue,
                                    @Param("today") LocalDate today,
                                    Pageable pageable);
}
