package com.architek.oikos.messaging.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.messaging.infrastructure.mapper.ConversationReadMarkerPersistenceMapperImpl;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ConversationReadMarkerRepositoryAdapter.class, ConversationReadMarkerPersistenceMapperImpl.class})
class ConversationReadMarkerRepositoryAdapterDataJpaTest {

    @Autowired
    private ConversationReadMarkerRepositoryAdapter adapter;

    @Test
    void a_marker_is_absent_until_saved() {
        assertThat(adapter.findByConversationIdAndUserId(ConversationId.newId(), EntityId.newId())).isEmpty();
    }

    @Test
    void saves_and_reloads_a_read_marker() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();
        MessageId lastReadMessageId = MessageId.newId();
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        adapter.save(ConversationReadMarker.unread(conversationId, userId).markRead(lastReadMessageId, now));

        ConversationReadMarker reloaded = adapter.findByConversationIdAndUserId(conversationId, userId).orElseThrow();
        assertThat(reloaded.getLastReadMessageId()).isEqualTo(lastReadMessageId);
        assertThat(reloaded.getLastReadAt()).isEqualTo(now);
    }

    @Test
    void saving_again_updates_the_same_marker_rather_than_creating_a_second_row() {
        ConversationId conversationId = ConversationId.newId();
        EntityId userId = EntityId.newId();
        adapter.save(ConversationReadMarker.unread(conversationId, userId).markRead(MessageId.newId(), Instant.EPOCH));

        MessageId secondMessageId = MessageId.newId();
        Instant later = Instant.parse("2026-01-01T12:00:00Z");
        adapter.save(ConversationReadMarker.unread(conversationId, userId).markRead(secondMessageId, later));

        ConversationReadMarker reloaded = adapter.findByConversationIdAndUserId(conversationId, userId).orElseThrow();
        assertThat(reloaded.getLastReadMessageId()).isEqualTo(secondMessageId);
        assertThat(reloaded.getLastReadAt()).isEqualTo(later);
    }
}
