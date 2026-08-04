package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.UpdateUnitSharesCommand;
import com.architek.oikos.property.application.dto.UnitView;

public interface UpdateUnitSharesUseCase {

    UnitView updateShares(UpdateUnitSharesCommand command);
}
