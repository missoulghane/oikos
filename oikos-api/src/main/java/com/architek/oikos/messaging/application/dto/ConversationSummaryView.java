package com.architek.oikos.messaging.application.dto;

import java.time.Instant;
import java.util.List;

import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * participants lists every participant of the conversation except the
 * caller, for GROUP (1..N entries); it is always empty for BROADCAST (the
 * "recipient" is the whole property, not specific users - the web
 * layer/frontend renders propertyName instead in that case). subject is the
 * GROUP conversation's title (see ConversationSubject); always null for
 * BROADCAST. lastMessagePreview/lastMessageAt/messageCount are never "empty"
 * in practice: both StartGroupConversationService and
 * SendBroadcastMessageService post a first message in the same transaction
 * that creates the conversation, so every conversation returned by
 * ListMyConversationsUseCase already has at least one message. messageCount
 * lets the UI distinguish a plain single message from an actual conversation
 * (a message with replies) - see Conversation's javadoc: composing/sending
 * never "starts a conversation" by itself, a second message does.
 */
public record ConversationSummaryView(ConversationId id, ConversationType type, EntityId propertyId, String propertyName,
                                       String subject, List<ConversationParticipantView> participants,
                                       String lastMessagePreview, Instant lastMessageAt, long unreadCount,
                                       long messageCount) {
}
