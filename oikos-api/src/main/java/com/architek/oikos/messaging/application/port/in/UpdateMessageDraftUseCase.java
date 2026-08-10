package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.UpdateMessageDraftCommand;

public interface UpdateMessageDraftUseCase {

    void update(UpdateMessageDraftCommand command);
}
