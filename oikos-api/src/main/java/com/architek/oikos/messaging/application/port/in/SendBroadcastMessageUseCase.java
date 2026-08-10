package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;

public interface SendBroadcastMessageUseCase {

    ConversationId send(SendBroadcastMessageCommand command);
}
