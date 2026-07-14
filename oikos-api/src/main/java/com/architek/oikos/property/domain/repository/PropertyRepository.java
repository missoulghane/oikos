package com.architek.oikos.property.domain.repository;

import java.util.Optional;

import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(PropertyId id);

    Page<Property> findAll(PageRequest pageRequest);
}
