package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.query.GetPropertyQuery;

public interface GetPropertyUseCase {

    PropertyView getProperty(GetPropertyQuery query);
}
