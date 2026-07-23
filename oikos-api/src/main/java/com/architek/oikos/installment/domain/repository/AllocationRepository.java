package com.architek.oikos.installment.domain.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface AllocationRepository {

    Allocation save(Allocation allocation);

    Optional<Allocation> findById(AllocationId id);

    void deleteById(AllocationId id);

    List<Allocation> findAllByInstallmentId(InstallmentId installmentId);

    List<Allocation> findAllByMovementId(EntityId movementId);

    /**
     * Zero (not empty/absent) when no allocation exists yet - callers subtract
     * this directly from a movement's or installment's amount.
     */
    BigDecimal sumAllocatedByMovementId(EntityId movementId);

    BigDecimal sumAllocatedByInstallmentId(InstallmentId installmentId);
}
