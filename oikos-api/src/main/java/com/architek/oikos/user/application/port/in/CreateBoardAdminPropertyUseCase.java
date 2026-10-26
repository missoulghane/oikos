package com.architek.oikos.user.application.port.in;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.CreateBoardAdminPropertyCommand;

public interface CreateBoardAdminPropertyUseCase {

    /** Returns the id of the property created. */
    EntityId create(CreateBoardAdminPropertyCommand command);
}
