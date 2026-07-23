package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.AddUnitTypeDefinitionCommand;
import com.architek.oikos.property.application.port.in.AddUnitTypeDefinitionUseCase;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.exception.UnitTypeNameAlreadyUsedException;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

@Component
public class AddUnitTypeDefinitionService implements AddUnitTypeDefinitionUseCase {

    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;
    private final PropertyRepository propertyRepository;

    public AddUnitTypeDefinitionService(UnitTypeDefinitionRepository unitTypeDefinitionRepository,
                                          PropertyRepository propertyRepository) {
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public UnitTypeDefinitionId add(AddUnitTypeDefinitionCommand command) {
        propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));

        if (unitTypeDefinitionRepository.existsByPropertyIdAndName(command.propertyId(), command.name())) {
            throw new UnitTypeNameAlreadyUsedException(command.name());
        }

        UnitTypeDefinition unitTypeDefinition = UnitTypeDefinition.create(UnitTypeDefinitionId.newId(),
                command.propertyId(), command.name());
        return unitTypeDefinitionRepository.save(unitTypeDefinition).getId();
    }
}
