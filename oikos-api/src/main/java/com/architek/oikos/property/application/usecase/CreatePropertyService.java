package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.application.port.out.PropertyAccountProvisioningPort;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.model.Building;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BuildingRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

/**
 * Cree une property, sa ligne de type de lot par defaut ("OTHERS") et, si
 * fourni, son premier building, dans la meme transaction. Le premier building
 * est optionnel au niveau de cette commande: un appelant peut creer une
 * property seule (ex: inscription d'un property manager, voir
 * PropertyProvisioningAdapter) et ajouter des buildings plus tard via
 * AddBuildingUseCase. Le endpoint public POST /properties, lui, continue
 * d'exiger un premier building via la validation de CreatePropertyRequest. La
 * ligne "OTHERS", elle, est toujours creee, sans condition.
 */
@Component
public class CreatePropertyService implements CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final BuildingRepository buildingRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final PropertyAccountProvisioningPort propertyAccountProvisioningPort;

    public CreatePropertyService(PropertyRepository propertyRepository, BuildingRepository buildingRepository,
                                  UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                  PropertyAccountProvisioningPort propertyAccountProvisioningPort) {
        this.propertyRepository = propertyRepository;
        this.buildingRepository = buildingRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.propertyAccountProvisioningPort = propertyAccountProvisioningPort;
    }

    @Override
    @Transactional
    public PropertyId create(CreatePropertyCommand command) {
        Property property = Property.create(PropertyId.newId(), command.name(), command.address());
        Property saved = propertyRepository.save(property);
        propertyAccountProvisioningPort.provisionAccount(saved.getId().value());

        unitTypeDefinitionRepository.save(UnitTypeDefinition.create(UnitTypeDefinitionId.newId(), saved.getId(),
                UnitTypeDefinition.DEFAULT_NAME));

        if (command.firstBuildingName() != null && !command.firstBuildingName().isBlank()) {
            Building building = Building.create(BuildingId.newId(), saved.getId(), command.firstBuildingName(),
                    command.firstBuildingFloorCount());
            buildingRepository.save(building);
        }

        return saved.getId();
    }
}
