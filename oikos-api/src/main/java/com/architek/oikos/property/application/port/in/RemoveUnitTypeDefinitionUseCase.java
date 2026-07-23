package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.RemoveUnitTypeDefinitionCommand;

public interface RemoveUnitTypeDefinitionUseCase {

    void remove(RemoveUnitTypeDefinitionCommand command);
}
