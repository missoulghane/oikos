package com.architek.oikos.property.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.ListPropertiesUseCase;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.shared.domain.pagination.Page;

@Component
public class ListPropertiesService implements ListPropertiesUseCase {

    private final PropertyRepository propertyRepository;

    public ListPropertiesService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PropertyView> listProperties(ListPropertiesQuery query) {
        return propertyRepository.findAll(query.pageRequest()).map(PropertyView::from);
    }
}
