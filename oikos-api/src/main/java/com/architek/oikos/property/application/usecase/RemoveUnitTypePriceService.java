package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.RemoveUnitTypePriceCommand;
import com.architek.oikos.property.application.port.in.RemoveUnitTypePriceUseCase;
import com.architek.oikos.property.domain.exception.UnitTypePriceNotFoundException;
import com.architek.oikos.property.domain.repository.UnitTypePricingRepository;

@Component
public class RemoveUnitTypePriceService implements RemoveUnitTypePriceUseCase {

    private final UnitTypePricingRepository unitTypePricingRepository;

    public RemoveUnitTypePriceService(UnitTypePricingRepository unitTypePricingRepository) {
        this.unitTypePricingRepository = unitTypePricingRepository;
    }

    @Override
    @Transactional
    public void remove(RemoveUnitTypePriceCommand command) {
        unitTypePricingRepository.findByUnitTypeId(command.unitTypeId())
                .filter(pricing -> pricing.getPropertyId().equals(command.propertyId()))
                .orElseThrow(() -> new UnitTypePriceNotFoundException(command.unitTypeId()));

        unitTypePricingRepository.deleteByUnitTypeId(command.unitTypeId());
    }
}
