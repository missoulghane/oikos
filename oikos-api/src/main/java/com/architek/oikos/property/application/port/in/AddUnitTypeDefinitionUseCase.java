package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddUnitTypeDefinitionCommand;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;

public interface AddUnitTypeDefinitionUseCase {

    UnitTypeDefinitionId add(AddUnitTypeDefinitionCommand command);
}
