package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.installment.domain.model.Allocation;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record AllocationView(AllocationId id, EntityId movementId, InstallmentId installmentId, BigDecimal allocatedAmount) {

    public static AllocationView from(Allocation allocation) {
        return new AllocationView(allocation.getId(), allocation.getMovementId(), allocation.getInstallmentId(),
                allocation.getAllocatedAmount().value());
    }
}
