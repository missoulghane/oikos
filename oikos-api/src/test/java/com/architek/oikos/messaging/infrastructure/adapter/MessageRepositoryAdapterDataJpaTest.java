package com.architek.oikos.messaging.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.messaging.infrastructure.mapper.MessagePersistenceMapperImpl;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({MessageRepositoryAdapter.class, MessagePersistenceMapperImpl.class})
class MessageRepositoryAdapterDataJpaTest {

    @Autowired
    private MessageRepositoryAdapter adapter;

    private Message post(ConversationId conversationId, EntityId senderId, String body, Instant createdDate) {
        return adapter.save(Message.post(MessageId.newId(), conversationId, senderId, SenderIdentity.OWNER, MessageBody.of(body),
                createdDate));
    }

    // Le fil se lit comme une boîte mail : le dernier message en tête.
    @Test
    void a_page_of_recent_messages_is_returned_most_recent_first() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        Message first = post(conversationId, sender, "1", Instant.parse("2026-01-01T10:00:00Z"));
        Message second = post(conversationId, sender, "2", Instant.parse("2026-01-01T10:01:00Z"));
        Message third = post(conversationId, sender, "3", Instant.parse("2026-01-01T10:02:00Z"));

        Page<Message> page = adapter.findRecentPage(conversationId, PageRequest.of(0, 50));

        assertThat(page.content()).extracting(Message::getId).containsExactly(third.getId(), second.getId(), first.getId());
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void the_second_page_holds_the_next_older_messages_in_the_same_order() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        for (int i = 0; i < 5; i++) {
            post(conversationId, sender, "msg" + i, Instant.parse("2026-01-01T10:00:00Z").plusSeconds(i));
        }

        Page<Message> firstPage = adapter.findRecentPage(conversationId, PageRequest.of(0, 3));
        Page<Message> secondPage = adapter.findRecentPage(conversationId, PageRequest.of(1, 3));

        assertThat(firstPage.content()).extracting(m -> m.getBody().value()).containsExactly("msg4", "msg3", "msg2");
        assertThat(secondPage.content()).extracting(m -> m.getBody().value()).containsExactly("msg1", "msg0");
    }

    @Test
    void finds_the_last_message_of_a_conversation() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        post(conversationId, sender, "old", Instant.parse("2026-01-01T10:00:00Z"));
        Message last = post(conversationId, sender, "new", Instant.parse("2026-01-01T11:00:00Z"));

        assertThat(adapter.findLastMessage(conversationId)).contains(last);
    }

    @Test
    void finding_the_last_message_of_an_empty_conversation_is_empty() {
        assertThat(adapter.findLastMessage(ConversationId.newId())).isEmpty();
    }

    @Test
    void counts_every_message_in_a_conversation() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        post(conversationId, sender, "1", Instant.parse("2026-01-01T10:00:00Z"));
        post(conversationId, sender, "2", Instant.parse("2026-01-01T10:01:00Z"));
        post(conversationId, sender, "3", Instant.parse("2026-01-01T10:02:00Z"));

        assertThat(adapter.countByConversation(conversationId)).isEqualTo(3);
    }

    @Test
    void counts_every_message_as_unread_when_never_read() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        post(conversationId, sender, "1", Instant.parse("2026-01-01T10:00:00Z"));
        post(conversationId, sender, "2", Instant.parse("2026-01-01T10:01:00Z"));

        assertThat(adapter.countUnread(conversationId, null)).isEqualTo(2);
    }

    @Test
    void counts_only_messages_from_the_given_sender() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        EntityId otherSender = EntityId.newId();
        post(conversationId, sender, "1", Instant.parse("2026-01-01T10:00:00Z"));
        post(conversationId, sender, "2", Instant.parse("2026-01-01T10:01:00Z"));
        post(conversationId, otherSender, "3", Instant.parse("2026-01-01T10:02:00Z"));

        assertThat(adapter.countByConversationAndSender(conversationId, sender)).isEqualTo(2);
        assertThat(adapter.countByConversationAndSender(conversationId, otherSender)).isEqualTo(1);
    }

    @Test
    void counts_only_messages_posted_after_the_last_read_message() {
        ConversationId conversationId = ConversationId.newId();
        EntityId sender = EntityId.newId();
        Message readMessage = post(conversationId, sender, "read", Instant.parse("2026-01-01T10:00:00Z"));
        post(conversationId, sender, "unread1", Instant.parse("2026-01-01T10:01:00Z"));
        post(conversationId, sender, "unread2", Instant.parse("2026-01-01T10:02:00Z"));

        assertThat(adapter.countUnread(conversationId, readMessage.getId())).isEqualTo(2);
    }
}
