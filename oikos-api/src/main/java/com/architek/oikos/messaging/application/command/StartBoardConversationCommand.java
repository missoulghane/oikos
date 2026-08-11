package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record StartBoardConversationCommand(EntityId propertyId, EntityId senderId, ConversationSubject subject,
                                             MessageBody body) {
}
