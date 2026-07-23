package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.SetUnitTypePriceCommand;
import com.architek.oikos.property.application.dto.UnitTypePriceView;
import com.architek.oikos.property.application.port.in.SetUnitTypePriceUseCase;
import com.architek.oikos.property.domain.exception.UnitTypeDefinitionNotFoundException;
import com.architek.oikos.property.domain.model.UnitTypeDefinition;
import com.architek.oikos.property.domain.model.UnitTypePricing;
import com.architek.oikos.property.domain.repository.UnitTypeDefinitionRepository;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;
import com.architek.oikos.property.domain.valueobject.UnitTypePricingId;

/**
 * Upsert: cree le prix s'il n'existait pas encore pour ce unitTypeId, ou met
 * a jour la valeur existante sinon (au plus une entree par type, cf.
 * UnitTypePricingRepository). Un unitTypeId inconnu ou appartenant a une
 * autre property est traite comme "not found" (pas de distinction, pour ne
 * pas reveler l'existence d'un id d'une autre property).
 */
@Component
public class SetUnitTypePriceService implements SetUnitTypePriceUseCase {

    private final UnitTypePricingRepository unitTypePricingRepository;
    private final UnitTypeDefinitionRepository unitTypeDefinitionRepository;

    public SetUnitTypePriceService(UnitTypePricingRepository unitTypePricingRepository,
                                     UnitTypeDefinitionRepository unitTypeDefinitionRepository) {
        this.unitTypePricingRepository = unitTypePricingRepository;
        this.unitTypeDefinitionRepository = unitTypeDefinitionRepository;
    }

    @Override
    @Transactional
    public UnitTypePriceView set(SetUnitTypePriceCommand command) {
        UnitTypeDefinition unitType = unitTypeDefinitionRepository.findById(command.unitTypeId())
                .filter(definition -> definition.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new UnitTypeDefinitionNotFoundException(command.unitTypeId()));

        UnitTypePricing unitTypePricing = unitTypePricingRepository.findByUnitTypeId(command.unitTypeId())
                .map(existing -> existing.withPrice(command.price()))
                .orElseGet(() -> UnitTypePricing.create(UnitTypePricingId.newId(), command.propertyId(),
                        command.unitTypeId(), command.price()));

        return UnitTypePriceView.from(unitTypePricingRepository.save(unitTypePricing), unitType.getName());
    }
}
