package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.CreateMessageDraftCommand;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;

public interface CreateMessageDraftUseCase {

    MessageDraftId create(CreateMessageDraftCommand command);
}
