package com.architek.oikos.property.application.usecase;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.BuildingConfiguration;
import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.application.command.UnitTypeConfiguration;
import com.architek.oikos.property.application.port.in.ConfigurePropertyUseCase;
import com.architek.oikos.property.application.port.out.PropertyAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.PropertyConfigurationLimitExceededException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.Shares;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Creates a property, its default "OTHERS" unit type, all of its buildings and
 * units in a single transaction (POST /properties/configure). Units are
 * created with Shares.ZERO - tantiemes are assigned later through a dedicated
 * flow, not at bulk creation time. Each UnitTypeConfiguration entry names a
 * unit type (find-or-create by (property, name), since the property does not
 * exist yet before this call - its types cannot preexist); unitNumber is
 * generated as "{name} {n}", restarting at 1 per (building, unitType): valid
 * only because buildings created by this use case start empty - it does not
 * coordinate with units added afterwards via AddUnitUseCase.
 */
@Component
public class ConfigurePropertyService implements ConfigurePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final PropertyAccountProvisioningPort propertyAccountProvisioningPort;
    private final int maxUnitsPerRequest;

    public ConfigurePropertyService(PropertyRepository propertyRepository,
                                     BuildingRepository buildingRepository,
                                     UnitRepository unitRepository,
                                     UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                     PropertyAccountProvisioningPort propertyAccountProvisioningPort,
                                     @Value("${oikos.property.configure.max-units}") int maxUnitsPerRequest) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.propertyAccountProvisioningPort = propertyAccountProvisioningPort;
        this.maxUnitsPerRequest = maxUnitsPerRequest;
    }

    @Override
    @Transactional
    public PropertyId configure(ConfigurePropertyCommand command) {
        int totalUnitCount = command.buildings().stream()
                .flatMap(building -> building.unitTypes().stream())
                .mapToInt(UnitTypeConfiguration::count)
                .sum();
        if (totalUnitCount > maxUnitsPerRequest) {
            throw new PropertyConfigurationLimitExceededException(totalUnitCount, maxUnitsPerRequest);
        }

        Property savedProperty = propertyRepository.save(
                Property.create(PropertyId.newId(), command.name(), command.address()));
        propertyAccountProvisioningPort.provisionAccount(savedProperty.getId().value());

        UnitTypeDefinition defaultUnitType = unitTypeDefinitionRepository.save(UnitTypeDefinition.create(
                UnitTypeDefinitionId.newId(), savedProperty.getId(), UnitTypeDefinition.DEFAULT_NAME));
        Map<String, UnitTypeDefinitionId> unitTypeIdsByName = new HashMap<>();
        unitTypeIdsByName.put(defaultUnitType.getName(), defaultUnitType.getId());

        for (BuildingConfiguration buildingConfiguration : command.buildings()) {
            Building savedBuilding = buildingRepository.save(Building.create(BuildingId.newId(),
                    savedProperty.getId(), buildingConfiguration.name(), buildingConfiguration.floorCount()));

            for (UnitTypeConfiguration unitTypeConfiguration : buildingConfiguration.unitTypes()) {
                UnitTypeDefinitionId unitTypeId = unitTypeIdsByName.computeIfAbsent(unitTypeConfiguration.unitTypeName(),
                        name -> unitTypeDefinitionRepository.save(
                                UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), savedProperty.getId(), name))
                                .getId());
                createUnits(savedBuilding.getId(), unitTypeConfiguration.unitTypeName(), unitTypeId,
                        unitTypeConfiguration.count());
            }
        }

        return savedProperty.getId();
    }

    private void createUnits(BuildingId buildingId, String unitTypeName, UnitTypeDefinitionId unitTypeId, int count) {
        for (int sequence = 1; sequence <= count; sequence++) {
            unitRepository.save(Unit.create(UnitId.newId(), buildingId, unitTypeName + " " + sequence, unitTypeId,
                    Shares.of(BigDecimal.ZERO)));
        }
    }
}
