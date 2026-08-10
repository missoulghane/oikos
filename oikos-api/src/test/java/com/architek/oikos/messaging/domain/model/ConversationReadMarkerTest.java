package com.architek.oikos.messaging.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

class ConversationReadMarkerTest {

    @Test
    void an_unread_marker_has_no_last_read_message_or_timestamp() {
        ConversationReadMarker marker = ConversationReadMarker.unread(ConversationId.newId(), EntityId.newId());

        assertThat(marker.getLastReadMessageId()).isNull();
        assertThat(marker.getLastReadAt()).isNull();
    }

    @Test
    void marking_read_records_the_last_read_message_and_timestamp() {
        ConversationReadMarker marker = ConversationReadMarker.unread(ConversationId.newId(), EntityId.newId());
        MessageId lastMessageId = MessageId.newId();
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        ConversationReadMarker read = marker.markRead(lastMessageId, now);

        assertThat(read.getLastReadMessageId()).isEqualTo(lastMessageId);
        assertThat(read.getLastReadAt()).isEqualTo(now);
    }

    @Test
    void two_markers_for_the_same_conversation_and_user_are_equal() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();

        ConversationReadMarker first = ConversationReadMarker.unread(conversationId, userId);
        ConversationReadMarker second = ConversationReadMarker.unread(conversationId, userId).markRead(MessageId.newId(), Instant.now());

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
