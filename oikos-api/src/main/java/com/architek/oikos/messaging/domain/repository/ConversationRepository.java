package com.architek.oikos.messaging.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.architek.oikos.messaging.domain.model.Conversation;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface ConversationRepository {

    Conversation save(Conversation conversation);

    Optional<Conversation> findById(ConversationId id);

    /** Every GROUP conversation (wherever created) the given user is currently a participant of. */
    List<Conversation> findAllGroupByParticipant(EntityId userId);

    /**
     * Every conversation of the given type across the given properties in one
     * query - used by ListMyConversationsService/GetUnreadSummaryService to
     * resolve every BROADCAST send and every BOARD_PRIVATE thread of every
     * property the caller is a member of (resp. staff of), rather than one
     * query per property.
     */
    List<Conversation> findAllByPropertyIdsAndType(Collection<EntityId> propertyIds, ConversationType type);
}
