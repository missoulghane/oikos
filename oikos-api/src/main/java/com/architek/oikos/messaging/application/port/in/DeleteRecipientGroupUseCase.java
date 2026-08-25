package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.DeleteRecipientGroupCommand;

public interface DeleteRecipientGroupUseCase {

    void delete(DeleteRecipientGroupCommand command);
}
