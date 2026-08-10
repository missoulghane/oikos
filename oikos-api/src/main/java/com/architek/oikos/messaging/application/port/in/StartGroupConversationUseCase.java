package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;

public interface StartGroupConversationUseCase {

    ConversationId start(StartGroupConversationCommand command);
}
