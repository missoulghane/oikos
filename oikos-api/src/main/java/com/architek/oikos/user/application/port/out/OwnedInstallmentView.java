package com.architek.oikos.user.application.port.out;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record OwnedInstallmentView(EntityId id, EntityId unitId, LocalDate dueDate, BigDecimal amount,
                                    BigDecimal outstandingAmount, OwnedInstallmentStatus status) {
}
