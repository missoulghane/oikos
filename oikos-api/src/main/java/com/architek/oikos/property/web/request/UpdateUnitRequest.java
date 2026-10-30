package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Same constraints as AddUnitRequest, minus the type, which a lot keeps for
 * life (its prices and its ledger account hang on it). floor stays optional; its
 * fit with the building is checked by UpdateUnitService.
 */
public record UpdateUnitRequest(
        @NotBlank @Size(max = 20) String unitNumber,
        @Min(0) Integer floor,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal shares) {
}
