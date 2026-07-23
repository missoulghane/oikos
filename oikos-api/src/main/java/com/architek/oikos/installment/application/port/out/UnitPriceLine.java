package com.architek.oikos.installment.application.port.out;

import java.math.BigDecimal;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One unit of a property with the price resolved from its UnitTypePricing
 * configuration, or a null price if that unit's type has none configured
 * (not an error - see property.domain.model.UnitTypePricing).
 */
public record UnitPriceLine(EntityId unitId, BigDecimal price) {
}
