package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.SetProjectedBudgetCommand;
import com.architek.oikos.property.application.dto.PropertyView;

public interface SetProjectedBudgetUseCase {

    PropertyView setProjectedBudget(SetProjectedBudgetCommand command);
}
