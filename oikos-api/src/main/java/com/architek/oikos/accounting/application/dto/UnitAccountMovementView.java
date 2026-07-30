package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.domain.model.UnitAccountMovement;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;

public record UnitAccountMovementView(UnitAccountMovementId id, LocalDate date, UnitAccountMovementType type,
                                       UnitAccountMovementDirection direction, BigDecimal amount, String label,
                                       String businessReference, String reason) {

    public static UnitAccountMovementView from(UnitAccountMovement movement) {
        return new UnitAccountMovementView(movement.getId(), movement.getDate(), movement.getType(),
                movement.getDirection(), movement.getAmount().value(), movement.getLabel(),
                movement.getBusinessReference(), movement.getReason());
    }
}
