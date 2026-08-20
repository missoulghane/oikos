package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.UpdateBuildingCommand;
import com.architek.oikos.property.application.dto.BuildingView;

public interface UpdateBuildingUseCase {

    BuildingView update(UpdateBuildingCommand command);
}
