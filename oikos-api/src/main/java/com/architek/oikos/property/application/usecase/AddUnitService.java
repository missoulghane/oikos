package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.out.UnitAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;

@Component
public class AddUnitService implements AddUnitUseCase {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final UnitAccountProvisioningPort unitAccountProvisioningPort;

    public AddUnitService(UnitRepository unitRepository, BuildingRepository buildingRepository,
                           UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                           UnitAccountProvisioningPort unitAccountProvisioningPort) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.unitAccountProvisioningPort = unitAccountProvisioningPort;
    }

    @Override
    @Transactional
    public UnitId add(AddUnitCommand command) {
        Building building = buildingRepository.findById(command.buildingId())
                .orElseThrow(() -> new BuildingNotFoundException(command.buildingId()));

        unitTypeDefinitionRepository.findById(command.unitTypeId())
                .filter(unitType -> unitType.getPropertyId().equals(building.getPropertyId()))
                .orElseThrow(() -> new UnitTypeDefinitionNotFoundException(command.unitTypeId()));

        Unit unit = Unit.create(UnitId.newId(), command.buildingId(), building.getPropertyId(), command.unitNumber(),
                command.unitTypeId(), Shares.of(command.shares()));
        Unit savedUnit = unitRepository.save(unit);

        unitAccountProvisioningPort.provisionAccount(savedUnit.getId().value(), savedUnit.getPropertyId().value());

        return savedUnit.getId();
    }
}
