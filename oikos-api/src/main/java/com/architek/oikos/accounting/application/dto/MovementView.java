package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.accounting.domain.model.Movement;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

public record MovementView(MovementId id, AccountId accountId, Instant occurredOn, MovementType type,
                               MovementDirection direction, BigDecimal amount, String label, String businessReference) {

    public static MovementView from(Movement movement) {
        return new MovementView(movement.getId(), movement.getAccountId(), movement.getOccurredOn(), movement.getType(),
                movement.getDirection(), movement.getAmount().value(), movement.getLabel(), movement.getBusinessReference());
    }
}
