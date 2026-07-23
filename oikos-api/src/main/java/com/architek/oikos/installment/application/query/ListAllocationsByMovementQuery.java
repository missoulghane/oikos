package com.architek.oikos.installment.application.query;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListAllocationsByMovementQuery(EntityId movementId) {
}
