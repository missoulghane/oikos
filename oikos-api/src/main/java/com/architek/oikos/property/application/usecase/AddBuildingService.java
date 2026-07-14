package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddBuildingCommand;
import com.architek.oikos.property.application.port.in.AddBuildingUseCase;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.BuildingId;

@Component
public class AddBuildingService implements AddBuildingUseCase {

    private final BuildingRepository buildingRepository;
    private final PropertyRepository propertyRepository;

    public AddBuildingService(BuildingRepository buildingRepository, PropertyRepository propertyRepository) {
        this.buildingRepository = buildingRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public BuildingId add(AddBuildingCommand command) {
        propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));
        Building building = Building.create(BuildingId.newId(), command.propertyId(), command.name(), command.floorCount());
        return buildingRepository.save(building).getId();
    }
}
