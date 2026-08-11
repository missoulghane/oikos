package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * How many units of one type a building holds. Unlike
 * UnitTypeConfigurationRequest (legacy POST /properties/configure) zero is
 * allowed: the wizard shows every type the property declared for each
 * building, and a type simply absent from that building is a legitimate 0.
 */
public record ConfiguredUnitCountRequest(
        @NotBlank @Size(max = 50) String unitTypeName,
        @NotNull @Min(0) Integer count) {
}
