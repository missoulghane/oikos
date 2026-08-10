package com.architek.oikos.messaging.domain.repository;

import java.util.Optional;

import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface ConversationReadMarkerRepository {

    ConversationReadMarker save(ConversationReadMarker marker);

    Optional<ConversationReadMarker> findByConversationIdAndUserId(ConversationId conversationId, EntityId userId);
}
