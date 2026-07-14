package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String address,
        @NotBlank @Size(max = 100) String firstBuildingName,
        @NotNull @Min(0) Integer firstBuildingFloorCount) {
}
