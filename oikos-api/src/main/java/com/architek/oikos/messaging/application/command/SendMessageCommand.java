package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record SendMessageCommand(ConversationId conversationId, EntityId senderId, MessageBody body) {
}
