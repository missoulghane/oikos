package com.architek.oikos.messaging.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<MessageEntity, UUID> {

    /** Descending order (most recent first) - MessageRepositoryAdapter reverses each page's content to ascending. */
    Page<MessageEntity> findByConversationIdOrderByCreatedDateDesc(UUID conversationId, Pageable pageable);

    Optional<MessageEntity> findFirstByConversationIdOrderByCreatedDateDesc(UUID conversationId);

    long countByConversationId(UUID conversationId);

    long countByConversationIdAndSenderId(UUID conversationId, UUID senderId);

    @Query("select count(m) from MessageEntity m where m.conversationId = :conversationId and m.createdDate > "
            + "(select r.createdDate from MessageEntity r where r.id = :lastReadMessageId)")
    long countByConversationIdAndCreatedDateAfterMessage(@Param("conversationId") UUID conversationId,
                                                          @Param("lastReadMessageId") UUID lastReadMessageId);
}
