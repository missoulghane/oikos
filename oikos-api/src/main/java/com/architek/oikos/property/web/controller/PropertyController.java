package com.architek.oikos.property.web.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.application.port.in.ConfigurePropertyUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.port.in.UpdatePropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.web.request.BuildingConfigurationRequest;
import com.architek.oikos.property.web.request.ConfigurePropertyRequest;
import com.architek.oikos.property.web.request.CreatePropertyRequest;
import com.architek.oikos.property.web.request.UnitTypeConfigurationRequest;
import com.architek.oikos.property.web.request.UpdatePropertyRequest;
import com.architek.oikos.property.web.response.PropertyResponse;
import com.architek.oikos.property.web.response.PagedPropertyResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;

/**
 * Gestion administrative des copropriétés
 */
// Pour le moment aucune de gestion de droits (à mettre en place plus tard)
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final ConfigurePropertyUseCase configurePropertyUseCase;
    private final GetPropertyUseCase getPropertyUseCase;
    private final ListPropertiesUseCase listPropertiesUseCase;
    private final UpdatePropertyUseCase updatePropertyUseCase;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase,
                                  ConfigurePropertyUseCase configurePropertyUseCase,
                                  GetPropertyUseCase getPropertyUseCase,
                                  ListPropertiesUseCase listPropertiesUseCase,
                                  UpdatePropertyUseCase updatePropertyUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.configurePropertyUseCase = configurePropertyUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
        this.listPropertiesUseCase = listPropertiesUseCase;
        this.updatePropertyUseCase = updatePropertyUseCase;
    }

    @GetMapping
    public PagedPropertyResponse list(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return PagedPropertyResponse.from(
                listPropertiesUseCase.listProperties(new ListPropertiesQuery(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    public PropertyResponse getById(@PathVariable String id) {
        return PropertyResponse.from(getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(id))));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePropertyRequest request) {
        PropertyId id = createPropertyUseCase.create(new CreatePropertyCommand(
                request.name(), request.address(), request.firstBuildingName(), request.firstBuildingFloorCount()));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    @PutMapping("/{id}")
    public PropertyResponse update(@PathVariable String id, @Valid @RequestBody UpdatePropertyRequest request) {
        return PropertyResponse.from(updatePropertyUseCase.update(
                new UpdatePropertyCommand(PropertyId.of(id), request.name(), request.address())));
    }

    @PostMapping("/configure")
    public ResponseEntity<Void> configure(@Valid @RequestBody ConfigurePropertyRequest request) {
        PropertyId id = configurePropertyUseCase.configure(toCommand(request));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + id)).build();
    }

    private static ConfigurePropertyCommand toCommand(ConfigurePropertyRequest request) {
        List<BuildingConfiguration> buildings = request.property().buildings().stream()
                .map(PropertyController::toBuildingConfiguration)
                .toList();
        return new ConfigurePropertyCommand(request.property().name(), request.property().address(), buildings);
    }

    private static BuildingConfiguration toBuildingConfiguration(BuildingConfigurationRequest request) {
        List<UnitTypeConfiguration> unitTypes = request.unitTypes().stream()
                .map(PropertyController::toUnitTypeConfiguration)
                .toList();
        return new BuildingConfiguration(request.name(), request.floorCount(), unitTypes);
    }

    private static UnitTypeConfiguration toUnitTypeConfiguration(UnitTypeConfigurationRequest request) {
        return new UnitTypeConfiguration(request.unitTypeName(), request.count());
    }
}
