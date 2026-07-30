package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.accounting.application.dto.LettrageMovementView;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementDirection;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementType;

public record LettrageMovementResponse(String id, LocalDate date, UnitAccountMovementType type,
                                        UnitAccountMovementDirection direction, BigDecimal amount, String label,
                                        BigDecimal remainingAmount) {

    public static LettrageMovementResponse from(LettrageMovementView view) {
        return new LettrageMovementResponse(view.movement().id().toString(), view.movement().date(),
                view.movement().type(), view.movement().direction(), view.movement().amount(), view.movement().label(),
                view.remainingAmount());
    }
}
