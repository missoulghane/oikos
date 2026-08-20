package com.architek.oikos.property.web.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PropertyConfigurationRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String address,
        @Size(max = 100) String city,
        @NotEmpty List<@Valid BuildingConfigurationRequest> buildings) {
}
