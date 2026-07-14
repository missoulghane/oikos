package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;

/**
 * Cree une property et son premier building dans la meme transaction:
 * la regle de gestion "une property doit posseder au moins un building" est
 * ainsi garantie des la creation, plutot que verifiee a posteriori.
 */
@Component
public class CreatePropertyService implements CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;

    public CreatePropertyService(PropertyRepository propertyRepository, BuildingRepository buildingRepository) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
    }

    @Override
    @Transactional
    public PropertyId create(CreatePropertyCommand command) {
        Property property = Property.create(PropertyId.newId(), command.name(), command.address());
        Property saved = propertyRepository.save(property);

        Building building = Building.create(BuildingId.newId(), saved.getId(), command.firstBuildingName(),
                command.firstBuildingFloorCount());
        buildingRepository.save(building);

        return saved.getId();
    }
}
