package com.architek.oikos.messaging.application.query;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;

public record GetConversationQuery(ConversationId conversationId) {
}
