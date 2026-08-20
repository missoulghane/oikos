package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.UpdatePropertyUseCase;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;

@Component
public class UpdatePropertyService implements UpdatePropertyUseCase {

    private final PropertyRepository propertyRepository;

    public UpdatePropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional
    public PropertyView update(UpdatePropertyCommand command) {
        Property property = propertyRepository.findById(command.id())
                .orElseThrow(() -> new PropertyNotFoundException(command.id()));
        Property updated = propertyRepository.save(property.withDetails(command.name(), command.address(), command.city()));
        return PropertyView.from(updated);
    }
}
