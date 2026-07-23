package com.architek.oikos.installment.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AllocationJpaRepository extends JpaRepository<AllocationEntity, UUID> {

    List<AllocationEntity> findAllByInstallmentId(UUID installmentId);

    List<AllocationEntity> findAllByMovementId(UUID movementId);

    @Query("select coalesce(sum(a.allocatedAmount), 0) from AllocationEntity a where a.movementId = :movementId")
    BigDecimal sumAllocatedAmountByMovementId(@Param("movementId") UUID movementId);

    @Query("select coalesce(sum(a.allocatedAmount), 0) from AllocationEntity a where a.installmentId = :installmentId")
    BigDecimal sumAllocatedAmountByInstallmentId(@Param("installmentId") UUID installmentId);
}
