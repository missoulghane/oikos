package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.command.UpdatePropertyCommand;
import com.architek.oikos.property.application.dto.PropertyView;

public interface UpdatePropertyUseCase {

    PropertyView update(UpdatePropertyCommand command);
}
