package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.CreateRecipientGroupCommand;
import com.architek.oikos.messaging.domain.valueobject.RecipientGroupId;

public interface CreateRecipientGroupUseCase {

    RecipientGroupId create(CreateRecipientGroupCommand command);
}
