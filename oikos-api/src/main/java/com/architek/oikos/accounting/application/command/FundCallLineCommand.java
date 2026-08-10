package com.architek.oikos.accounting.application.command;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

public record FundCallLineCommand(EntityId unitId, BigDecimal amount) {
}
