package com.architek.oikos.messaging.infrastructure.persistence;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key (conversation_id, user_id), same @IdClass pattern as
 * RolePermissionEntity. Created lazily (upsert) by
 * MarkConversationReadService - never seeded upfront.
 */
@Entity
@Table(name = "conversation_read_marker")
@IdClass(ConversationReadMarkerEntity.ConversationReadMarkerKey.class)
@Getter
@Setter
@NoArgsConstructor
public class ConversationReadMarkerEntity {

    @Id
    @Column(name = "conversation_id")
    private UUID conversationId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    public static class ConversationReadMarkerKey implements Serializable {
        private UUID conversationId;
        private UUID userId;

        public ConversationReadMarkerKey() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o instanceof ConversationReadMarkerKey other
                    && Objects.equals(conversationId, other.conversationId) && Objects.equals(userId, other.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(conversationId, userId);
        }
    }
}
