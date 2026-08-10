package com.architek.oikos.messaging.application.query;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public record ListConversationMessagesQuery(ConversationId conversationId, EntityId userId, PageRequest pageRequest) {
}
