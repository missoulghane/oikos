package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddUnitOwnerCommand;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

public interface AddUnitOwnerUseCase {

    UnitOwnershipId add(AddUnitOwnerCommand command);
}
