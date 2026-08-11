package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.ConfigureExistingPropertyCommand;

public interface ConfigureExistingPropertyUseCase {

    void configure(ConfigureExistingPropertyCommand command);
}
