package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.RemoveUnitOwnershipCommand;

public interface RemoveUnitOwnershipUseCase {

    void remove(RemoveUnitOwnershipCommand command);
}
