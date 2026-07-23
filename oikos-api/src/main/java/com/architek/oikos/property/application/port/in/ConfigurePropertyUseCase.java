package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.ConfigurePropertyCommand;
import com.architek.oikos.property.domain.valueobject.PropertyId;

public interface ConfigurePropertyUseCase {

    PropertyId configure(ConfigurePropertyCommand command);
}
