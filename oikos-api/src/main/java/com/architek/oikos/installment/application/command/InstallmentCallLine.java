package com.architek.oikos.installment.application.command;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record InstallmentCallLine(EntityId unitId, BigDecimal amount) {
}
