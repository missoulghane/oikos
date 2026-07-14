package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.domain.valueobject.PropertyId;

public interface CreatePropertyUseCase {

    PropertyId create(CreatePropertyCommand command);
}
