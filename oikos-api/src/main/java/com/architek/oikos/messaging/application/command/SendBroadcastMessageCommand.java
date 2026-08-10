package com.architek.oikos.messaging.application.command;

import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record SendBroadcastMessageCommand(EntityId propertyId, EntityId senderId, MessageBody body) {
}
