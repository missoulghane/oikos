package com.architek.oikos.messaging.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(ConversationId id);

    Optional<Conversation> findBroadcastConversation(EntityId propertyId);

    /** Every GROUP conversation (wherever created) the given user is currently a participant of. */
    List<Conversation> findAllGroupByParticipant(EntityId userId);

    /**
     * Batch counterpart of {@link #findBroadcastConversation(EntityId)}, used by
     * ListMyConversationsService/GetUnreadSummaryService to resolve every
     * BROADCAST channel of every property the caller is a member of in one
     * query rather than one per property.
     */
    List<Conversation> findAllBroadcastByPropertyIds(Collection<EntityId> propertyIds);
}
