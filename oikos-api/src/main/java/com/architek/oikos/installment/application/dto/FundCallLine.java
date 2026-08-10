package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record FundCallLine(EntityId unitId, BigDecimal amount) {
}
