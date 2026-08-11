package com.architek.oikos.property.web.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A unit type the property declares, with its flat-rate price when there is
 * one. price stays optional: it is meaningless in SHARES mode, and a wizard
 * may legitimately be finished before the amounts are known.
 */
public record ConfiguredUnitTypeRequest(
        @NotBlank @Size(max = 50) String name,
        @DecimalMin("0.00") BigDecimal price) {
}
