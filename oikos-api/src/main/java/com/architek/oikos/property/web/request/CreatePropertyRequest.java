package com.architek.oikos.property.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 250) String address) {
}
