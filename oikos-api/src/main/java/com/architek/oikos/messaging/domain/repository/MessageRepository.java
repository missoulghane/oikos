package com.architek.oikos.messaging.domain.repository;

import java.util.Optional;

import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.pagination.PageRequest;

public interface MessageRepository {

    Message save(Message message);

    /**
     * Page 0 = the most recent pageSize messages, page 1 = the next-older
     * pageSize, etc. (pagination walks backwards from "now") - but the
     * content of each individual page is returned in ASCENDING chronological
     * order (oldest of that page first), ready to render top-to-bottom
     * without the caller having to reverse it.
     */
    Page<Message> findRecentPage(ConversationId conversationId, PageRequest pageRequest);

    Optional<Message> findLastMessage(ConversationId conversationId);

    /**
     * Total number of messages in the conversation - lets the UI distinguish
     * a plain single message from an actual conversation (a message with
     * replies), rather than treating every conversation envelope as a "chat
     * thread" by default.
     */
    long countByConversation(ConversationId conversationId);

    /**
     * Number of messages posted after lastReadMessageId (exclusive) - or every
     * message in the conversation if lastReadMessageId is null (never read).
     */
    long countUnread(ConversationId conversationId, MessageId lastReadMessageId);
}
