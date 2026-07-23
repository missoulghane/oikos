package com.architek.oikos.property.web.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BuildingConfigurationRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) Integer floorCount,
        @NotEmpty List<@Valid UnitTypeConfigurationRequest> unitTypes) {
}
