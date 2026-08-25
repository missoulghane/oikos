package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.UpdateRecipientGroupCommand;

public interface UpdateRecipientGroupUseCase {

    void update(UpdateRecipientGroupCommand command);
}
