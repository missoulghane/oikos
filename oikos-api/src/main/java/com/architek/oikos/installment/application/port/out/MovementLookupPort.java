package com.architek.oikos.installment.application.port.out;

import java.util.Optional;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used by AllocatePaymentService (manual allocation) to
 * validate a movement referenced by a payment allocation, without depending
 * on accounting's repositories directly (rule 4). Implemented in
 * installment.infrastructure.adapter by delegating to accounting's public
 * port-in (FindMovementUseCase).
 */
public interface MovementLookupPort {

    Optional<MovementInfo> findMovement(EntityId movementId);
}
