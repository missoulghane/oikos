package com.architek.oikos.accounting.web.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.accounting.application.dto.MovementView;
import com.architek.oikos.accounting.domain.valueobject.MovementDirection;
import com.architek.oikos.accounting.domain.valueobject.MovementType;

public record MovementResponse(String id, String accountId, Instant occurredOn, MovementType type,
                                   MovementDirection direction, BigDecimal amount, String label, String businessReference) {

    public static MovementResponse from(MovementView view) {
        return new MovementResponse(view.id().toString(), view.accountId().toString(), view.occurredOn(), view.type(),
                view.direction(), view.amount(), view.label(), view.businessReference());
    }
}
