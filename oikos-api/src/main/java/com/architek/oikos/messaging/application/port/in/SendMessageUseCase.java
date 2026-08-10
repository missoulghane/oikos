package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.SendMessageCommand;
import com.architek.oikos.messaging.application.dto.MessageView;

public interface SendMessageUseCase {

    MessageView send(SendMessageCommand command);
}
