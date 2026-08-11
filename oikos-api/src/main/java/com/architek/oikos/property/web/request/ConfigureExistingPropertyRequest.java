package com.architek.oikos.property.web.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;

/**
 * What the volunteer-syndic wizard commits from its recap screen (steps 3 to
 * 6). bankAccounts may be empty: declaring a bank account is explicitly
 * skippable in the wizard.
 */
public record ConfigureExistingPropertyRequest(
        @NotNull DuesCalculationMode duesCalculationMode,
        @DecimalMin("0.00") BigDecimal projectedBudget,
        @NotEmpty List<@Valid ConfiguredUnitTypeRequest> unitTypes,
        @NotEmpty List<@Valid ConfiguredBuildingRequest> buildings,
        List<@Valid ConfiguredBankAccountRequest> bankAccounts) {
}
