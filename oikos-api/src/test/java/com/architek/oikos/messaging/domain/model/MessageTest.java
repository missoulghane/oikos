package com.architek.oikos.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class MessageTest {

    @Test
    void posting_a_message_captures_sender_identity_body_and_timestamp() {
        MessageId id = MessageId.newId();
        ConversationId conversationId = ConversationId.newId();
        EntityId senderId = EntityId.newId();
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        Message message = Message.post(id, conversationId, senderId, SenderIdentity.BOARD, MessageBody.of("Bonjour"), now);

        assertThat(message.getId()).isEqualTo(id);
        assertThat(message.getConversationId()).isEqualTo(conversationId);
        assertThat(message.getSenderId()).isEqualTo(senderId);
        assertThat(message.getSenderIdentity()).isEqualTo(SenderIdentity.BOARD);
        assertThat(message.getBody().value()).isEqualTo("Bonjour");
        assertThat(message.getCreatedDate()).isEqualTo(now);
    }

    @Test
    void two_messages_with_the_same_id_are_equal() {
        MessageId id = MessageId.newId();
        Message first = Message.post(id, ConversationId.newId(), EntityId.newId(), SenderIdentity.OWNER, MessageBody.of("a"),
                Instant.EPOCH);
        Message second = Message.post(id, ConversationId.newId(), EntityId.newId(), SenderIdentity.OWNER, MessageBody.of("b"),
                Instant.EPOCH);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
