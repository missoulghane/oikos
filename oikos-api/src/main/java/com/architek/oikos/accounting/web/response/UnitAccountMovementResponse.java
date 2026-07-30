package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.UnitAccountMovementView;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;

public record UnitAccountMovementResponse(String id, LocalDate date, UnitAccountMovementType type,
                                           UnitAccountMovementDirection direction, BigDecimal amount, String label,
                                           String businessReference, String reason) {

    public static UnitAccountMovementResponse from(UnitAccountMovementView view) {
        return new UnitAccountMovementResponse(view.id().toString(), view.date(), view.type(), view.direction(),
                view.amount(), view.label(), view.businessReference(), view.reason());
    }
}
