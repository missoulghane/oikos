package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One unit of a property with its configured shares (tantiemes), used to
 * prorate a property's projected budget in SHARES dues-calculation mode.
 */
public record UnitShareLine(EntityId unitId, BigDecimal shares) {
}
