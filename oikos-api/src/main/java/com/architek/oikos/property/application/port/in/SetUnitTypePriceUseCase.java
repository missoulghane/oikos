package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.SetUnitTypePriceCommand;
import com.architek.oikos.property.application.dto.UnitTypePriceView;

public interface SetUnitTypePriceUseCase {

    UnitTypePriceView set(SetUnitTypePriceCommand command);
}
