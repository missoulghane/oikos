package com.architek.oikos.property.application.command;

import java.math.BigDecimal;
import java.util.List;

import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.PropertyId;

/**
 * Everything steps 3 to 6 of the volunteer-syndic wizard collected, committed
 * in one go when the recap is validated. projectedBudget is only meaningful in
 * SHARES mode, unit type prices only in FLAT_RATE mode - both are optional so
 * the wizard can be finished before those numbers are known.
 */
public record ConfigureExistingPropertyCommand(PropertyId propertyId, DuesCalculationMode duesCalculationMode,
                                                BigDecimal projectedBudget, List<UnitTypePricing> unitTypes,
                                                List<BuildingConfiguration> buildings,
                                                List<BankAccountConfiguration> bankAccounts) {

    public record UnitTypePricing(String name, BigDecimal price) {
    }

    public record BankAccountConfiguration(String label, String bankAccountNumber) {
    }
}
