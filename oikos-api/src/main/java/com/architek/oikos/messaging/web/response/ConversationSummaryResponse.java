package com.architek.oikos.messaging.web.response;

import java.time.Instant;
import java.util.List;

import com.architek.oikos.messaging.application.dto.ConversationSummaryView;
import com.architek.oikos.messaging.domain.model.ConversationType;

public record ConversationSummaryResponse(String id, ConversationType type, String propertyId, String propertyName,
                                           String subject, List<ConversationParticipantResponse> participants,
                                           String lastMessagePreview, Instant lastMessageAt, long unreadCount,
                                           long messageCount) {

    public static ConversationSummaryResponse from(ConversationSummaryView view) {
        return new ConversationSummaryResponse(view.id().toString(), view.type(), view.propertyId().toString(), view.propertyName(),
                view.subject(), view.participants().stream().map(ConversationParticipantResponse::from).toList(),
                view.lastMessagePreview(), view.lastMessageAt(), view.unreadCount(), view.messageCount());
    }
}
