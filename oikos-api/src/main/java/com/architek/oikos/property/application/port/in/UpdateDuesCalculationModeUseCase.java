package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.UpdateDuesCalculationModeCommand;
import com.architek.oikos.property.application.dto.PropertyView;

public interface UpdateDuesCalculationModeUseCase {

    PropertyView updateMode(UpdateDuesCalculationModeCommand command);
}
