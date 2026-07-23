package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddUnitTypeDefinitionRequest(
        @NotBlank @Size(max = 50) String name) {
}
