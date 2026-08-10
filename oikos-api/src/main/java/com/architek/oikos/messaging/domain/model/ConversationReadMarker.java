package com.architek.oikos.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Per-user, per-conversation read cursor: how far a given user has read a
 * given conversation. Created lazily on first access (upsert), including for
 * a BROADCAST channel never opened before - there is no "one row per
 * conversation" seeded upfront, MarkConversationReadService creates the row
 * the first time a user marks a conversation read. lastReadMessageId/
 * lastReadAt are both null until the first mark-as-read call.
 * Immutable value-ish entity: identity is the (conversationId, userId) pair,
 * every mutation returns a new instance.
 */
public final class ConversationReadMarker {

    private final ConversationId conversationId;
    private final EntityId userId;
    private final MessageId lastReadMessageId;
    private final Instant lastReadAt;

    private ConversationReadMarker(ConversationId conversationId, EntityId userId, MessageId lastReadMessageId,
                                    Instant lastReadAt) {
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.lastReadMessageId = lastReadMessageId;
        this.lastReadAt = lastReadAt;
    }

    public static ConversationReadMarker unread(ConversationId conversationId, EntityId userId) {
        return new ConversationReadMarker(conversationId, userId, null, null);
    }

    public static ConversationReadMarker reconstruct(ConversationId conversationId, EntityId userId,
                                                       MessageId lastReadMessageId, Instant lastReadAt) {
        return new ConversationReadMarker(conversationId, userId, lastReadMessageId, lastReadAt);
    }

    public ConversationReadMarker markRead(MessageId lastReadMessageId, Instant lastReadAt) {
        return new ConversationReadMarker(conversationId, userId, lastReadMessageId, lastReadAt);
    }

    public ConversationId getConversationId() {
        return conversationId;
    }

    public EntityId getUserId() {
        return userId;
    }

    /** Nullable - null means the conversation has never been marked read by this user. */
    public MessageId getLastReadMessageId() {
        return lastReadMessageId;
    }

    /** Nullable - null means the conversation has never been marked read by this user. */
    public Instant getLastReadAt() {
        return lastReadAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof ConversationReadMarker other
                && conversationId.equals(other.conversationId) && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, userId);
    }
}
