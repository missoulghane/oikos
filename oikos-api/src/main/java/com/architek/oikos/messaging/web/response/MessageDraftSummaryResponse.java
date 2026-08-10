package com.architek.oikos.messaging.web.response;

import java.time.Instant;
import java.util.List;

import com.architek.oikos.messaging.application.dto.MessageDraftSummaryView;

public record MessageDraftSummaryResponse(String id, String propertyId, String propertyName, boolean broadcast,
                                           List<ConversationParticipantResponse> recipients, String subject, String body,
                                           Instant lastModifiedAt) {

    public static MessageDraftSummaryResponse from(MessageDraftSummaryView view) {
        return new MessageDraftSummaryResponse(view.id().toString(), view.propertyId().toString(), view.propertyName(),
                view.broadcast(), view.recipients().stream().map(ConversationParticipantResponse::from).toList(),
                view.subject(), view.body(), view.lastModifiedAt());
    }
}
