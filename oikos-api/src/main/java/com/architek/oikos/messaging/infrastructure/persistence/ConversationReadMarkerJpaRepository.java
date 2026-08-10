package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.messaging.infrastructure.persistence.ConversationReadMarkerEntity.ConversationReadMarkerKey;

public interface ConversationReadMarkerJpaRepository extends JpaRepository<ConversationReadMarkerEntity, ConversationReadMarkerKey> {

    Optional<ConversationReadMarkerEntity> findByConversationIdAndUserId(UUID conversationId, UUID userId);
}
