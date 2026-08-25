package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.application.port.out.LedgerAccountProvisioningPort;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.exception.UnitFloorOutOfBuildingRangeException;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;

/** Provisions the new unit's dedicated PCM receivable account (ADR 0001 decision 5, "exigence supplementaire"). */
@Component
public class AddUnitService implements AddUnitUseCase {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final LedgerAccountProvisioningPort ledgerAccountProvisioningPort;

    public AddUnitService(UnitRepository unitRepository, BuildingRepository buildingRepository,
                           UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                           LedgerAccountProvisioningPort ledgerAccountProvisioningPort) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.ledgerAccountProvisioningPort = ledgerAccountProvisioningPort;
    }

    @Override
    @Transactional
    public UnitId add(AddUnitCommand command) {
        Building building = buildingRepository.findById(command.buildingId())
                .orElseThrow(() -> new BuildingNotFoundException(command.buildingId()));

        unitTypeDefinitionRepository.findById(command.unitTypeId())
                .filter(unitType -> unitType.getPropertyId().equals(building.getPropertyId()))
                .orElseThrow(() -> new UnitTypeDefinitionNotFoundException(command.unitTypeId()));

        // L'etage reste facultatif ; fourni, il doit exister dans cet immeuble-la.
        // 0 est le rez-de-chaussee, d'ou la borne haute inclusive : un immeuble
        // "3 etages" contient bien les etages 0 a 3.
        if (command.floor() != null && command.floor() > building.getFloorCount()) {
            throw new UnitFloorOutOfBuildingRangeException(command.floor(), building.getFloorCount());
        }

        Unit unit = Unit.create(UnitId.newId(), command.buildingId(), building.getPropertyId(), command.unitNumber(),
                command.unitTypeId(), Shares.of(command.shares()), command.floor());
        Unit savedUnit = unitRepository.save(unit);

        ledgerAccountProvisioningPort.provisionUnitReceivableAccount(savedUnit.getPropertyId().value(),
                savedUnit.getId().value());

        return savedUnit.getId();
    }
}
