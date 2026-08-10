package com.architek.oikos.messaging.application.command;

import java.util.Set;

import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record StartGroupConversationCommand(EntityId propertyId, EntityId senderId, Set<EntityId> recipientUserIds,
                                             ConversationSubject subject, MessageBody body) {
}
