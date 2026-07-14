package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;

@Component
public class GetPropertyService implements GetPropertyUseCase {

    private final PropertyRepository propertyRepository;

    public GetPropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyView getProperty(GetPropertyQuery query) {
        Property property = propertyRepository.findById(query.id())
                .orElseThrow(() -> new PropertyNotFoundException(query.id()));
        return PropertyView.from(property);
    }
}
