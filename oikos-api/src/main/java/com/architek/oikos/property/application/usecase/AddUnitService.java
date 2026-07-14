package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.application.port.in.AddUnitUseCase;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.Shares;

@Component
public class AddUnitService implements AddUnitUseCase {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;

    public AddUnitService(UnitRepository unitRepository, BuildingRepository buildingRepository) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional
    public UnitId add(AddUnitCommand command) {
        buildingRepository.findById(command.buildingId())
                .orElseThrow(() -> new BuildingNotFoundException(command.buildingId()));
        Unit unit = Unit.create(UnitId.newId(), command.buildingId(), command.unitNumber(), command.unitType(),
                Shares.of(command.shares()));
        return unitRepository.save(unit).getId();
    }
}
