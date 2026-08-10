package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.DeleteMessageDraftCommand;

public interface DeleteMessageDraftUseCase {

    void delete(DeleteMessageDraftCommand command);
}
