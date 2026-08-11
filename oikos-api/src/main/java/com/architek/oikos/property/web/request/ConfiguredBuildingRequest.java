package com.architek.oikos.property.web.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * name and floorCount are optional here, unlike the legacy
 * BuildingConfigurationRequest: the wizard never asks for a floor count, and
 * naming a building is offered as a convenience. The controller supplies the
 * defaults ("Bâtiment n", 0) before the domain, which requires both, ever sees
 * the values.
 */
public record ConfiguredBuildingRequest(
        @Size(max = 100) String name,
        @Min(0) Integer floorCount,
        @NotNull List<@Valid ConfiguredUnitCountRequest> unitTypes) {
}
