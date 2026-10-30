package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.UpdateUnitCommand;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.UpdateUnitUseCase;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.exception.UnitFloorOutOfBuildingRangeException;
import com.architek.oikos.property.domain.exception.UnitNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.Unit;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitOwnershipRepository;
import com.architek.oikos.property.domain.repository.UnitRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.Shares;

/**
 * Corrects a lot: its number, its floor and its shares. The type is not
 * editable here - prices and the lot's ledger account hang on it. The floor
 * follows the same rule as at creation (AddUnitService): optional, and when
 * given it must exist in the lot's building.
 */
@Component
public class UpdateUnitService implements UpdateUnitUseCase {

    private final UnitRepository unitRepository;
    private final BuildingRepository buildingRepository;
    private final UnitOwnershipRepository unitOwnershipRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public UpdateUnitService(UnitRepository unitRepository, BuildingRepository buildingRepository,
                             UnitOwnershipRepository unitOwnershipRepository,
                             UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitRepository = unitRepository;
        this.buildingRepository = buildingRepository;
        this.unitOwnershipRepository = unitOwnershipRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional
    public UnitView update(UpdateUnitCommand command) {
        Unit unit = unitRepository.findById(command.id())
                .orElseThrow(() -> new UnitNotFoundException(command.id()));
        if (command.floor() != null) {
            Building building = buildingRepository.findById(unit.getBuildingId())
                    .orElseThrow(() -> new BuildingNotFoundException(unit.getBuildingId()));
            if (command.floor() > building.getFloorCount()) {
                throw new UnitFloorOutOfBuildingRangeException(command.floor(), building.getFloorCount());
            }
        }
        Unit updated = unitRepository.save(
                unit.withDetails(command.unitNumber(), command.floor(), Shares.of(command.shares())));

        boolean hasCoproprietaires = !unitOwnershipRepository.findAllByUnitId(updated.getId()).isEmpty();
        String unitTypeName = unitTypeDefinitionRepository.findById(updated.getUnitTypeId())
                .map(UnitTypeDefinition::getName)
                .orElse(null);
        return UnitView.from(updated, hasCoproprietaires, unitTypeName);
    }
}
