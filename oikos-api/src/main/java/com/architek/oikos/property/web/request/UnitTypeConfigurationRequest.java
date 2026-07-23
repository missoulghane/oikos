package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnitTypeConfigurationRequest(
        @NotBlank @Size(max = 50) String unitTypeName,
        @NotNull @Min(1) Integer count) {
}
