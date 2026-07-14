package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.AddUnitOwnershipCommand;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;

public interface AddUnitOwnershipUseCase {

    UnitOwnershipId add(AddUnitOwnershipCommand command);
}
