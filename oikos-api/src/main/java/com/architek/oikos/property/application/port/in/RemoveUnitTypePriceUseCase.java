package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.RemoveUnitTypePriceCommand;

public interface RemoveUnitTypePriceUseCase {

    void remove(RemoveUnitTypePriceCommand command);
}
