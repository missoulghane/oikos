package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A credit movement of an account, as needed by FIFO auto-allocation - only
 * what AutoAllocationEngine matches against due installments, nothing else
 * from accounting's own Movement model.
 */
public record AccountMovement(EntityId id, Instant occurredOn, BigDecimal amount) {
}
