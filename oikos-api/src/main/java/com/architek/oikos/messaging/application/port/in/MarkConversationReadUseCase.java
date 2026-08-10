package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.MarkConversationReadCommand;

public interface MarkConversationReadUseCase {

    void markRead(MarkConversationReadCommand command);
}
