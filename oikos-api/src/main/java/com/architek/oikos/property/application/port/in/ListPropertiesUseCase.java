package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.query.ListPropertiesQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListPropertiesUseCase {

    Page<PropertyView> listProperties(ListPropertiesQuery query);
}
