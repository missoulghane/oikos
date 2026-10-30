package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.UpdateUnitCommand;
import com.architek.oikos.property.application.dto.UnitView;

public interface UpdateUnitUseCase {

    UnitView update(UpdateUnitCommand command);
}
