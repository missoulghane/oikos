package com.architek.oikos.property.application.command;

import java.math.BigDecimal;

import com.architek.oikos.property.domain.valueobject.UnitId;

public record UpdateUnitSharesCommand(UnitId id, BigDecimal shares) {
}
