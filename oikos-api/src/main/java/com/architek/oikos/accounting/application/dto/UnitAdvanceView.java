package com.architek.oikos.accounting.application.dto;

import java.math.BigDecimal;

import com.architek.oikos.accounting.domain.model.AuxiliaryUnitBalance;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record UnitAdvanceView(EntityId unitId, BigDecimal amount) {

    public static UnitAdvanceView from(AuxiliaryUnitBalance balance) {
        return new UnitAdvanceView(balance.unitId(), balance.amount());
    }
}
