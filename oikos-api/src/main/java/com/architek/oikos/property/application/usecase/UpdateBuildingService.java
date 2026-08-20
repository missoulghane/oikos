package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.UpdateBuildingCommand;
import com.architek.oikos.property.application.dto.BuildingView;
import com.architek.oikos.property.application.port.in.UpdateBuildingUseCase;
import com.architek.oikos.property.domain.exception.BuildingNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.BuildingRepository;

@Component
public class UpdateBuildingService implements UpdateBuildingUseCase {

    private final BuildingRepository buildingRepository;

    public UpdateBuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional
    public BuildingView update(UpdateBuildingCommand command) {
        Building building = buildingRepository.findById(command.id())
                .orElseThrow(() -> new BuildingNotFoundException(command.id()));
        Building updated = buildingRepository.save(building.withDetails(command.name(), command.floorCount()));
        return BuildingView.from(updated);
    }
}
