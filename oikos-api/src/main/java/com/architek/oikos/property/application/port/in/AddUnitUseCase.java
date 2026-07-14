package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddUnitCommand;
import com.architek.oikos.property.domain.valueobject.UnitId;

public interface AddUnitUseCase {

    UnitId add(AddUnitCommand command);
}
