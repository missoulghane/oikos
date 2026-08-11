package com.architek.oikos.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * A single message posted to a Conversation. Immutable history record (same
 * rationale as accounting's Movement): never edited, never deleted, no
 * lifecycle beyond "exists". createdDate is set at creation time by the
 * issuing service (via Clock), not read back from persistence audit columns,
 * since a Message needs its own timestamp immediately (message ordering,
 * unread computation) rather than only after a round-trip to the DB.
 * senderIdentity is the hat the sender chose to post under (see
 * SenderIdentity) - a property of this individual message, not of the
 * conversation, since a GROUP thread between a copropriétaire and the board
 * can legitimately see the same person reply as OWNER on one message and as
 * BOARD on the next.
 */
public final class Message {

    private final MessageId id;
    private final ConversationId conversationId;
    private final EntityId senderId;
    private final SenderIdentity senderIdentity;
    private final MessageBody body;
    private final Instant createdDate;

    private Message(MessageId id, ConversationId conversationId, EntityId senderId, SenderIdentity senderIdentity,
                     MessageBody body, Instant createdDate) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.conversationId = Objects.requireNonNull(conversationId, "conversationId must not be null");
        this.senderId = Objects.requireNonNull(senderId, "senderId must not be null");
        this.senderIdentity = Objects.requireNonNull(senderIdentity, "senderIdentity must not be null");
        this.body = Objects.requireNonNull(body, "body must not be null");
        this.createdDate = Objects.requireNonNull(createdDate, "createdDate must not be null");
    }

    public static Message post(MessageId id, ConversationId conversationId, EntityId senderId,
                                SenderIdentity senderIdentity, MessageBody body, Instant createdDate) {
        return new Message(id, conversationId, senderId, senderIdentity, body, createdDate);
    }

    public static Message reconstruct(MessageId id, ConversationId conversationId, EntityId senderId,
                                       SenderIdentity senderIdentity, MessageBody body, Instant createdDate) {
        return new Message(id, conversationId, senderId, senderIdentity, body, createdDate);
    }

    public MessageId getId() {
        return id;
    }

    public ConversationId getConversationId() {
        return conversationId;
    }

    public EntityId getSenderId() {
        return senderId;
    }

    public SenderIdentity getSenderIdentity() {
        return senderIdentity;
    }

    public MessageBody getBody() {
        return body;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Message other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
