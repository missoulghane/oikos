package com.architek.oikos.property.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ConfigurePropertyRequest(@NotNull @Valid PropertyConfigurationRequest property) {
}
