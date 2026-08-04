package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.UpdateDuesCalculationModeCommand;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.UpdateDuesCalculationModeUseCase;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;

@Component
public class UpdateDuesCalculationModeService implements UpdateDuesCalculationModeUseCase {

    private final PropertyRepository propertyRepository;

    public UpdateDuesCalculationModeService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public PropertyView updateMode(UpdateDuesCalculationModeCommand command) {
        Property property = propertyRepository.findById(command.id())
                .orElseThrow(() -> new PropertyNotFoundException(command.id()));
        Property updated = propertyRepository.save(property.withDuesCalculationMode(command.mode()));
        return PropertyView.from(updated);
    }
}
