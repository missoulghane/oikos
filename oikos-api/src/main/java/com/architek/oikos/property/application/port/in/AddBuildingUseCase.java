package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddBuildingCommand;
import com.architek.oikos.property.domain.valueobject.BuildingId;

public interface AddBuildingUseCase {

    BuildingId add(AddBuildingCommand command);
}
