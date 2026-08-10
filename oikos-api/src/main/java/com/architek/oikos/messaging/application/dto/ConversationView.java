package com.architek.oikos.messaging.application.dto;

import java.util.Set;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Internal, authorization-oriented projection of a Conversation - consumed by
 * PropertyAccessEvaluator.isConversationParticipant, which needs only type/
 * propertyId/participantUserIds to decide access (rule 6: cross-feature
 * access only through a port-in view, never the domain model directly).
 */
public record ConversationView(ConversationId id, EntityId propertyId, ConversationType type, EntityId createdBy,
                                Set<EntityId> participantUserIds) {

    public static ConversationView from(Conversation conversation) {
        return new ConversationView(conversation.getId(), conversation.getPropertyId(), conversation.getType(),
                conversation.getCreatedBy(), conversation.getParticipantUserIds());
    }
}
