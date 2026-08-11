package com.architek.oikos.messaging.application.dto;

import java.time.Instant;

import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/** mine is resolved by the use case (senderId == the caller's own userId), never by the web layer. */
public record MessageView(MessageId id, ConversationId conversationId, EntityId senderId, String senderName,
                           SenderIdentity senderIdentity, String body, Instant createdAt, boolean mine) {
}
