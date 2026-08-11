package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.StartBoardConversationCommand;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;

public interface StartBoardConversationUseCase {

    ConversationId start(StartBoardConversationCommand command);
}
